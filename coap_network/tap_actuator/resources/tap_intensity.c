#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

#include "global_variables.h"
static bool isActive = true;
static bool takesWaterFromAquifer = true;

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

static void get_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void post_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void intensity_event_handler(void);


EVENT_RESOURCE(tap_intensity,
               "title=\"Tap intensity\";obs",
               get_intensity_handler,
               post_switch_handler,
               put_intensity_handler,
               NULL,
               intensity_event_handler);

static void get_intensity_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling tap intensity get request...\n");
    char* msg;
    
    int length = snprintf(NULL, 0,"%lf", intensity) + 1;
    msg = (char*)malloc((length)*sizeof(char));
    snprintf(msg, length, "%lf", intensity);

    
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, length);
    
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
        char* chunk = strtok((char*)payload, " ");
        char* where = (char*)malloc((strlen(chunk))*sizeof(char));
        strcpy(where, chunk);

        chunk = strtok(NULL, " ");
        char* eptr;
        intensity = strtod(chunk, &eptr);
        printf("where: %s, new_value: %f\n", where, new_value);

        if (strncmp(where, "A", 1)==0)
        {
            takesWaterFromAquifer = true;
        }
        else if (strncmp(where, "R", 1)==0)
        {
            takesWaterFromAquifer = false;
        }
        free(where);
        
        printf("Tap intensity changed to %lf\n", intensity);
    } else
        success = false;

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


static void post_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling switch post request...\n");

    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        if (strncmp((char*)payload, "ON", strlen("ON")) == 0)
        {
            isActive = true;
            LOG_INFO("Switch on\n");
        }
        if (strncmp((char*)payload, "OFF", strlen("OFF")) == 0)
        {
            isActive = false;
            LOG_INFO("Switch off\n");
        }
    } else
        success = false;
    
    if(!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}
