#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include <sys/node-id.h>
#include "mqtt-client.h"
#include "reservoir_parameters.h"

#include <string.h>
#include <strings.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "re_level_detector"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT         1883


// We assume that the broker does not require authentication


/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT              0
#define STATE_NET_OK          1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(re_level_detector_process);
AUTOSTART_PROCESSES(&re_level_detector_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];

/***********************************************SIMULATION PARAMETERS*****************************************/
static int sensed_level = 50;
static int available = 5;

// Periodic timer to check the state of the MQTT client
static struct etimer periodic_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/
PROCESS(re_level_detector_process, "Reservoir Level Detector");



/*--------------------------handles incoming messages------------------------------------------*/
static void pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{
  LOG_INFO("Message received: topic='%s' (len=%u), chunk_len=%u\n", topic, topic_len, chunk_len);
  if(strcmp(topic, "interval") == 0) {
    printf("Changing detection interval to: ");

    long interval = atol((const char*)chunk);
    printf("%ld\n", interval);
    PUBLISH_INTERVAL = interval*CLOCK_SECOND;
  } else {
      LOG_ERR("Topic not recognized!\n");
  }
}

static int simulate_res_level(){
    return 60;
}


/*---------------------------------------------------------------------------*/
static void mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data){

  switch(event)
  {
      case MQTT_EVENT_CONNECTED:
      {
          LOG_INFO("MQTT connection acquired\n");
          state = STATE_CONNECTED;
          break;
      }
      case MQTT_EVENT_DISCONNECTED:
      {
          printf("MQTT connection disconnected. Reason: %u\n", *((mqtt_event_t *)data));
          state = STATE_DISCONNECTED;
          process_poll(&re_level_detector_process);
          break;
      }
      case MQTT_EVENT_PUBLISH:
      {
          msg_ptr = data;
          pub_handler(msg_ptr->topic, strlen(msg_ptr->topic), msg_ptr->payload_chunk, msg_ptr->payload_length);
          break;
      }
      case MQTT_EVENT_SUBACK:
      {
          #if MQTT_311
          mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;
          if(suback_event->success)
          {
              LOG_INFO("Application has subscribed to the topic successfully\n");
          }
          else
          {
              LOG_ERR("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
          }
          #else
          LOG_INFO("Application has subscribed to the topic\n");
          #endif
          break;
      }
      case MQTT_EVENT_UNSUBACK:
      {
          LOG_INFO("Application is unsubscribed to topic successfully\n");
          break;
      }
      case MQTT_EVENT_PUBACK:
      {
          LOG_INFO("Publishing complete.\n");
          break;
      }
      default:
          LOG_INFO("Application got a unhandled MQTT event: %i\n", event);
          break;
  }
}

static bool have_connectivity(void)
{
  bool problem1 = uip_ds6_get_global(ADDR_PREFERRED) == NULL;
  bool problem2 = uip_ds6_defrt_choose() == NULL;
  printf("problem1 is: %s, ", problem1 ? "true" : "false");
  printf("problem2 is: %s\n", problem2 ? "true" : "false");
  if( problem1|| problem2) {
    return false;
  }
  return true;
}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(re_level_detector_process, ev, data)
{

  PROCESS_BEGIN();

  mqtt_status_t status;
  char broker_address[CONFIG_IP_ADDR_STR_LEN];

  printf("Re Level Detector Process\n");

  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  // Broker registration
  printf("Try to register\n");
  mqtt_register(&conn, &re_level_detector_process, client_id, mqtt_event, MAX_TCP_SEGMENT_SIZE);

  printf("Registered\n");
  state=STATE_INIT;

  // Initialize periodic timer to check the status
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

  /* Main loop */
  printf("I'm in main while\n");
  while(1) {
    PROCESS_YIELD();

    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) ||
          ev == PROCESS_EVENT_POLL){

          if(state==STATE_INIT){
             if(have_connectivity()==true){
                 state = STATE_NET_OK;
                 LOG_INFO("STATE=STATE_NET_OK\n");
             }
             else
                LOG_INFO("STATE=STATE_INIT\n");
          }

          if(state == STATE_NET_OK){
              // Connect to MQTT server
              LOG_INFO("Connecting to MQTT server\n");

              memcpy(broker_address, broker_ip, strlen(broker_ip));

              mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
                           (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
                           MQTT_CLEAN_SESSION_ON);
              state = STATE_CONNECTING;
              LOG_INFO("STATE=STATE_CONNECTING\n");
          }

          if(state==STATE_CONNECTED){

              // Subscribe to a topic
              strcpy(sub_topic,"interval");

              status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

              printf("Subscribing!\n");
              if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
                LOG_ERR("Tried to subscribe but command queue was full!\n");
                PROCESS_EXIT();
              }
              state = STATE_SUBSCRIBED;
              PUBLISH_INTERVAL = (10*CLOCK_SECOND);
              STATE_MACHINE_PERIODIC = PUBLISH_INTERVAL;
              printf("STATE=STATE_SUBSCRIBED\n");
          } else if(state == STATE_SUBSCRIBED){

              LOG_INFO("I try to publish a message\n");
              sensed_level = simulate_res_level();
              sprintf(pub_topic, "%s", "re_level");

            //Assuming rectangular aquifer, available water is given by LEVEL * SECTION * WATER_SPEED * INTERVAL
              available = sensed_level*SECTION*WATER_SPEED*(PUBLISH_INTERVAL/CLOCK_SECOND); // SE TOLTA QUESTA RIGA NON VA DI NUOVO
              sprintf(app_buffer, "{\"node\": %d, \"reservoir_availability\": %d, \"unit\": \"cm^3\"}", node_id, available);
              mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer, strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
              printf("Sensed water level is: %d cm, aquifer water availability is %d cm^3\n", sensed_level, available);
              STATE_MACHINE_PERIODIC = PUBLISH_INTERVAL;

        } else if ( state == STATE_DISCONNECTED ){
              LOG_ERR("Disconnected form MQTT broker\n");
              state = STATE_INIT;
        }

        etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

    }

  }

  PROCESS_END();
}