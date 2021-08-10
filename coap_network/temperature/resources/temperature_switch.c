#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/*          RESOURCES            */
#include "global_variables.h"


/*          HANDLERS          */
static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(temperature_switch,
               "title=\"Temperature switch\";rf=\"switch\"",
               NULL,
               NULL,
               put_switch_handler,
               NULL);

static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling switch put request...\n");

    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        if (strncmp((char*)payload, "ON") == 0)
        {
            isActive = true;
            LOG_INFO("Switch on\n");
        }
        if (strncmp((char*)payload, "OFF") == 0)
        {
            isActive = false;
            LOG_INFO("Switch off\n");
        }
    } else
        success = !success;
}
