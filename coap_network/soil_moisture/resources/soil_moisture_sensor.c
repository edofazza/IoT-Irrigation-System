#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/**************** RESOURCES **********************/
#include "gloabal_variables.h"


static void get_soil_moisture_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void put_soil_moisture_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void soil_moisture_event_handler(void);

EVENT_RESOURCE(soil_moisture_sensor,
               "title=\"Soil_moisture sensor\";obs",
               get_soil_moisture_handler,
               NULL,
               put_soil_moisture_handler,
               NULL,
               soil_moisture_event_handler);

static void get_soil_moisture_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling soil moisture get request...\n");
    char* msg;
    
    // IF TOO HOT OR TOO COLD SEND A WARNING
    if (soilTension < LOWER_BOUND_SOIL_TENSION)
    {
        LOG_INFO("Tension lower than normal\n");
        static const int length = snprintf(NULL, 0,"%lf", soilTension) + sizeof("WARN low") + 1;
        msg = new char[length];
        snprintf(msg, length, "WARN low %lf", soilTension);
    }
    else if (soilTension > UPPER_BOUND_SOIL_TENSION)
    {
        LOG_INFO("Tension greater than normal\n");
        static const int length = snprintf(NULL, 0,"%lf", soilTension) + sizeof("WARN high") + 1;
        msg = new char[length];
        snprintf(msg, length, "WARN high %lf", soilTension);
    }
    else
    {
        static const int max_char_len = snprintf(NULL, 0,"%lf", soilTension) + 1;
        msg = new char[max_char_len];
        snprintf(msg, max_char_len, "%lf", soilTension);
    }
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    
    // COAP FUNCTIONS
    coap_set_header_content_format(response, TEXT_PLAIN);
    coap_set_header_etag(response, (uint8_t *)&len, 1);
    coap_set_payload(response, buffer, len);
}

static void put_soil_moisture_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling soil moisture put request...\n");
    
    size_t len = 0;
    const uint8_t* payload = NULL;
    bool success = true;
    
    if((len = coap_get_payload(request, &payload)))
    {
        char* chunks = strtok((char*)payload, " ");
        
        double new_value;
        sscanf(chunks[1], "%lf", new_value);
        if (strncmp(chunks[0], "u", strlen("u")))
        {
            if (new_value < LOWER_BOUND_SOIL_TENSION)
                success = false;
            else
                UPPER_BOUND_SOIL_TENSION = new_value;
        }
        else // update the lower bound
        {
            if (new_value > UPPER_BOUND_SOIL_TENSION)
                success = false;
            else
                LOWER_BOUND_SOIL_TENSION = new_value;
        }
    }

    if(!success)
        coap_set_status_code(response, BAD_REQUEST_4_00);
}

static void soil_moisture_event_handler(void)
{
    if (!isActive) {
        return; // DOES NOTHING SINCE IT IS TURNED OFF
    }
    
    // extimate new tension
    srand(time(NULL));
    int new_soilTension = soilTension;
    int random = rand() % 4; // generate 0, 1, 2, 3
    
    if (random == 0) // 25% of changing the value
        if (random < 2) // decrease
            new_soilTension -= VARIATION;
        else // increase
            new_soilTension += VARIATION;

    // if not equal
    if (new_soilTension != soilTension)
    {
        soilTension = new_soilTension;
        coap_notify_observers(&soil_moisture_sensor);
    }
}