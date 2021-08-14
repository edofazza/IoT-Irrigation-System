#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <time.h>
#include <errno.h>
#include "coap-engine.h"
#include "dev/leds.h"
#include "sys/log.h"

/* Log configuration */
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_APP

/**************** RESOURCES **********************/
#include "global_variables.h"

#define VARIATION 0.02

static double LOWER_BOUND_SOIL_TENSION = -0.60; // bar
static double UPPER_BOUND_SOIL_TENSION = -0.40; // bar

static double soilTension = -0.50;



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
    //char* msg;
    char msg[128];
    len = 128;
    max_char_len = len;
    // IF TOO HOT OR TOO COLD SEND A WARNING
    if (soilTension < LOWER_BOUND_SOIL_TENSION)
    {
        LOG_INFO("Tension lower than normal\n");
        //int length = snprintf(NULL, 0,"%lf", soilTension) + sizeof("WARN low") + 1;
        //msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "WARN low %.2lf", soilTension);
    }
    else if (soilTension > UPPER_BOUND_SOIL_TENSION)
    {
        LOG_INFO("Tension greater than normal\n");
        //int length = snprintf(NULL, 0,"%lf", soilTension) + sizeof("WARN high") + 1;
        //msg = (char*)malloc((length)*sizeof(char));
        snprintf(msg, length, "WARN high %.2lf", soilTension);
    }
    else
    {
        //int max_char_len = snprintf(NULL, 0,"%lf", soilTension) + 1;
        //msg = (char*)malloc((max_char_len)*sizeof(char));
        snprintf(msg, max_char_len, "%.2lf", soilTension);
    }
    
    // prepare buffer
    size_t len = strlen(msg);
    memcpy(buffer, (const void *)msg, len);
    //free(msg);
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
        char* chunk = strtok((char*)payload, " ");
        char* type = (char*)malloc((strlen(chunk))*sizeof(char));
        strcpy(type, chunk);

        chunk = strtok(NULL, " ");
        double new_value;
        char* eptr;
        new_value=strtod(chunk, &eptr);
        printf("type: %s, new_value: %f\n", type, new_value);

        if (strncmp(type, "u", 1)==0)
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
        free(type);
    }
    printf("LOWER B: %f, UPPER B: %f\n", LOWER_BOUND_SOIL_TENSION, UPPER_BOUND_SOIL_TENSION);

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
    double new_soilTension = soilTension;
    int random = rand() % 8; // generate 0, 1, 2, 3, 4, 5, 6, 7

        if (random <2) {// 25% of changing the value
            if (random == 0) // decrease
                new_soilTension -= VARIATION;
            else // increase
                new_soilTension += VARIATION;
        }
    
    // if not equal
    if (new_soilTension != soilTension)
    {
        soilTension = new_soilTension;
        coap_notify_observers(&soil_moisture_sensor);
    }
}
