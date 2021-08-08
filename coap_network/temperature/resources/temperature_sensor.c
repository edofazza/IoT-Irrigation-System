#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/**************** RESOURCES **********************/
static int LOWER_BOUND_LEVEL = 20;
static int UPPER_BOUND_LEVEL = 30;

static int temperature = 24;
static bool isActive = true;


/**************** REST: Temperature **********************/
static void get_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
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
    
}

static void put_temperature_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static void temperature_event_handler(void)
{
    
}

/**************** REST: Temperature **********************/
static void put_temperature_activate_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(temperature_act_sensor,
               "title=\"Temperature sensor act\";rf=\"Control\"",
               NULL,
               NULL,
               put_temperature_activate_handler,
               NULL,
               NULL);

static void put_temperature_activate_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    
}
