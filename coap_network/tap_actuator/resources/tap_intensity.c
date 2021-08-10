#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

#include "gloabal_variables.h"


static void get_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void intensity_event_handler(void);

EVENT_RESOURCE(tap_intensity,
               "title=\"Tap intensity\";obs",
               get_intensity_handler,
               NULL,
               put_intensity_handler,
               NULL,
               intensity_event_handle);

static void get_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling tap intensity get request...\n");
    char* msg;
    
    if (takesWaterFromAquifer)
    {
        static const int length = snprintf(NULL, 0,"%lf A", intensity) + 1;
        msg = new char[length];
        snprintf(msg, length, "%lf A", intensity);
    }
    else
    {
        static const int length = snprintf(NULL, 0,"%lf R", intensity) + 1;
        msg = new char[length];
        snprintf(msg, length, "%lf R
                 ", intensity);
    }
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void put_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling intensity put request...\n");
    
    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        sscanf(payload, "%lf", intensity);
        printf("Tap intensity changed to %lf\n", intensity);
    } else
        success = !success;

    if(!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}

static void intensity_event_handler(void)
{
    // tells to the client that "intensity" amount of water
    // has been used. This is done for simulation purposes
    if (isActive)
        coap_notify_observers(&tap_intensity);
}
