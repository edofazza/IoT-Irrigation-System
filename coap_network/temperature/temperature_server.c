#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "contiki.h"
#include "sys/etimer.h"
//#include "dev/leds.h"

#include "node-id.h"
#include "net/ipv6/simple-udp.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-ds6.h"
#include "net/ipv6/uip-debug.h"
#include "routing/routing.h"

#include "coap-engine.h"
#include "coap-blocking-api.h"

#define SERVER_EP "coap://[fd00::1]:5683"
#define CONN_TRY_INTERVAL 1
#define REG_TRY_INTERVAL 1
#define SIMULATION_INTERVAL 1
#define SENSOR_TYPE "temperature_sensor"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP


PROCESS(temperature_server, "Server for the temperature sensor");
AUTOSTART_PROCESSES(&temperature_server);

//*************************** GLOBAL VARIABLES *****************************//
char* service_url = "/registration";

static bool connected = false;
static bool registered = false;

static struct etimer wait_connectivity;
static struct etimer wait_registration;
static struct etimer simulation;

extern coap_resource_t temperature_sensor;

//*************************** UTILITY FUNCTIONS *****************************//
static void check_connection()
{
    if (!NETSTACK_ROUTING.node_is_reachable())
    {
        LOG_INFO("BR not reachable\n");
        etimer_reset(%wait_connectivity)
    }
    else
    {
        LOG_INFO("BR reachable");
        // TODO: notificare in qualche modo che si è connessi
        // gli altri hanno usato i led
        connected = true;
    }
}

void client_chunk_handler(coap_message_t *resource)
{
    const uint8_t* chunk:
    
    if (response == NULL)
    {
        LOG_INFO("Request timed out\n");
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL);
        return;
    }
    
    int len = coap_get_payload(response, &chunk);
    
    if(strncmp((char*)chunk, "Success", len) == 0)
        registered = true;
    else
        etimer_set(&wait_registration, CLOCK_SECOND * REG_TRY_INTERVAL)
}


//*************************** THREAD *****************************//
PROCESS_THREAD(temperature_server, ev, data)
{
    static coap_endpoint_t server_ep;
    static coap_message_t request[1];
    
    etimer_set(&wait_connectivity, CLOCK_SECOND * CONN_TRY_INTERVAL);
    
    while (!connected) {
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_connectivity));
        check_connection();
    }
    LOG_INFO("CONNECTED\n");
    
    while (!registered) {
        LOG_INFO("Sending registration message\n");
        
        coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);
        
        coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
        coap_set_header_uri_path(request, service_url);
        coap_set_payload(request, (uint8_t*) SENSOR_TYPE, sizeof(SENSOR_TYPE) - 1);
        COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
        
        // wait for the timer to expire
        PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));
    }
    LOG_INFO("REGISTERED\nStarting temperature server");
    
    coap_activate_resource(&temperature_sensor, "temperature_sensor");
    
    // SIMULATION
    etimer_set(&simulation, CLOCK_SECOND * SIMULATION_INTERVAL);
    LOG_INFO("Simulation\n");
    
    while (1) {
        PROCESS_WAIT_EVENT();
        
        if (ev == PROCESS_EVENT_TIMER && data == &simulation) {
            temperature_sensor.trigger();
            etimer_set(&simulation, CLOCK_SECOND * SIMULATION_INTERVAL);
        }
    }
    
    PROCESS_END();
}