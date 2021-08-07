#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

static void get_presence_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void presence_event_handler(void);

EVENT_RESOURCE(temperature_sensor,
               "title=\"Temperature sensor\";obs",
               get_presence_handler,
               NULL,
               NULL,
               NULL,
               presence_event_handler);

static void get_presence_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    
}

static void presence_event_handler(void)
{
    
}
