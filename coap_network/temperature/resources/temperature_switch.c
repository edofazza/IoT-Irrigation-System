#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/*          RESOURCES            */
#include "gloabal_variables.h"


/*          HANDLERS          */
static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(temperature_act_sensor,
               "title=\"Temperature switch\";rf=\"Control\"",
               NULL,
               NULL,
               put_switch_handler,
               NULL,
               NULL);

static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    
}
