#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/*          RESOURCES            */
#include "global_variables.h"


/*          HANDLERS          */
static void get_where_water_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_where_water_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(tap_interval,
               "title=\"Tap where water\";rf=\"where water\"",
               get_where_water_handler,
               NULL,
               put_where_water_handler,
               NULL);

static void get_where_water_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling where water get request...\n");
    char* msg;
    
    if (takesWaterFromAquifer)
        msg = "aquifer";
    else
        msg = "reservoir";
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void put_where_water_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling where water put request...\n");
    
    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        if (strncmp((char*)chunk, "A", strlen("A")) == 0)
        {
            takesWaterFromAquifer = true;
            LOG_INFO("Where to take water updated to aquifer");
        }
        else if (strncmp((char*)chunk, "R", strlen("R")) == 0)
        {
            takesWaterFromAquifer = false;
            LOG_INFO("Where to take water updated to reservoir");
        }
        else
        {
            success = !success;
            LOG_INFO("Not valid message");
        }
    } else
        success = !success;

    if(!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}