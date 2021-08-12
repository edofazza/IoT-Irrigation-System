#include <stdlib.h>
#include <string.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/*          RESOURCES            */
#include "global_variables.h"


/*          HANDLERS          */
static void get_interval_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_interval_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

EVENT_RESOURCE(tap_interval,
               "title=\"Tap interval\";rf=\"interval\"",
               get_interval_handler,
               NULL,
               put_switch_handler,
               NULL,
               NULL);

static void get_interval_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling interval get request...\n");
    char* msg;
    
    static const int length = snprintf(NULL, 0,"%d", interval) + 1;
    msg = new char[length];
    snprintf(msg, length, "%d", interval);
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling intensity put request...\n");
    
    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        int new_interval = atoi((char*)payload);
        intensity *= (double)new_interval/(double)interval;
 
        printf("Tap interval changed to %d\n", new_interval);
        printf("Tap intensity changed to %lf\n", intensity);
        interval = new_interval;
    } else
        success = !success;

    if(!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}
