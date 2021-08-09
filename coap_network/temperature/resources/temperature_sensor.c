#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/**************** RESOURCES **********************/
#include "gloabal_variables.h"


/**************** REST: Temperature **********************/
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void temperature_event_handler(void);

EVENT_RESOURCE(temperature_sensor,
               "title=\"Temperature sensor\";obs",
               get_temperature_handler,
               NULL,
               put_temperature_handler,
               NULL,
               temperature_event_handler);

static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature get request...\n");
   
    static const size_t max_char_len = 4; //-dd\0
    char* msg;
    
    // IF TOO HOT OR TOO COLD SEND A WARNING
    if (temperature < LOWER_BOUND_TEMP)
    {
        LOG_INFO("Temperature lower than normal\n");
        msg = "WARN hot";
    }
    else if (temperature > UPPER_BOUND_TEMP)
    {
        LOG_INFO("Temperature greater than normal\n");
        msg = "WARN cold";
    }
    else
    {
        msg = new char[max_char_len];
        snprintf(msg, max_char_len, "%d", temperature);
    }
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling temperature put request...\n");
    
    // TODO: DECIDERE IL FORMATO DELLA RICHIESTA
    // AGGIORNARE:
        //LOWER_BOUND_TEMP = new_lower_bound_value;
        //UPPER_BOUND_TEMP = new_upper_bound_value;
}

static void temperature_event_handler(void)
{
    if (!isActive) {
        return; // DOES NOTHING SINCE IT IS TURNED OFF
    }
    
    // extimate new temperature
    srand(time(NULL));
    int new_temp;
    int random = rand() % 4; // generate 0, 1, 2, 3
    
    if (random == 0) // 25% of changing the value
        if (random < 2) // decrease
            temperature -= VARIATION;
        else // increase
            temperature += VARIATION;

    // if not equal
    if (new_temp != temperature)
    {
        temperature = new_temp;
        coap_notify_observers(&temperature_sensor);
    }
}
