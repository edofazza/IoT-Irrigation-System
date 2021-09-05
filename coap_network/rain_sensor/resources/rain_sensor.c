#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/**************** RESOURCES **********************/
static bool isRaining = false;


/**************** REST: Rain **********************/
static void get_rain_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void rain_event_handler(void);

EVENT_RESOURCE(rain_sensor,
               "<\rain_sensor>;title=\"Rain sensor\";obs",
               get_rain_status_handler,
               NULL,
               NULL,
               NULL,
               rain_event_handler);

static void get_rain_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling rain get request...\n");
   
    // TODO: IF TOO HOT OR TOO COLD SEND A WARNING
    
    // SEND RAIN VALUE
    char* msg;
    if (isRaining)
    {
        int length = sizeof("raining") + 1;
        msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "raining");
    }
    else
    {
        int length = sizeof("not raining") + 1;
        msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "not raining");
    }
    
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void rain_event_handler(void)
{
    LOG_INFO("[RAIN] event\n");
    // check if raining
    srand(time(NULL));
    int random = rand() % 2; // generate 0, 1
    
    bool new_isRaining = isRaining;
    if (random == 0) { // 50% of changing the value
        new_isRaining = !isRaining;
        LOG_INFO("[RAIN] switched\n");
    } else {
        LOG_INFO("[RAIN] not switched\n");
    }
    // if not equal, notify
    if (new_isRaining != isRaining) {
        isRaining = new_isRaining;
        coap_notify_observers(&rain_sensor);
    }
}
