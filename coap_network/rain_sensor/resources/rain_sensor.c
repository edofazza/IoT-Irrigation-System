#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/**************** RESOURCES **********************/
#include "gloabal_variables.h"


/**************** REST: Rain **********************/
static void get_rain_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void rain_event_handler(void);

EVENT_RESOURCE(temperature_sensor,
               "title=\"Rain sensor\";obs",
               get_rain_status_handler,
               NULL,
               NULL,
               NULL,
               rain_event_handler);

static void get_rain_status_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature get request...\n");
   
    // TODO: IF TOO HOT OR TOO COLD SEND A WARNING
    
    // SEND TEMPERATURE VALUE
    char* msg;
    if (isRaining)
        msg = "raining";
    else
        msg = "not raining";
    
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void rain_event_handler(void)
{
    // check if raining
    srand(time(NULL));
    int new_temp;
    int random = rand() % 10; // generate 0, 1, ..., 9
    
    if (random == 0) // 10% of changing the value
        isRaining = !isRaining;

    // if not equal, notify
    if (isRaining != isRaining)
        coap_notify_observers(&temperature_sensor);
}
