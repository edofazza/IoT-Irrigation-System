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
               "title=\"Temperature switch\";rf=\"switch\"",
               NULL,
               NULL,
               put_switch_handler,
               NULL);

static void put_switch_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
    LOG_INFO("Handling switch put request...\n");

    const char *rcvd_msg = NULL;
    const size_t max_char_len = 3;
    char char_on_off[max_char_len];
    size_t len = 0;

    len = coap_get_post_variable(request, "status", &rcvd_msg);

    if (len > 0 && len <= max_char_len) {
      // correct len
      snprintf(char_on_off, max_char_len + 1, "%s", rcvd_msg);  // +1 = end string

      if (strcmp(char_on_off, "ON") == 0) {
        // correct ON request, notify new status
        isActive = true;
        LOG_INFO("Switch on\n");

        coap_set_status_code(response, CHANGED_2_04);

        char msg[max_char_len];

        snprintf(msg, max_char_len, "%s", "ON");

        size_t len = strlen(msg);
        memcpy(buffer, (const void *)msg, len);

        coap_set_header_content_format(response, TEXT_PLAIN);
        coap_set_header_etag(response, (uint8_t *)&len, 1);
        coap_set_payload(response, buffer, len);
      }

      if (strcmp(char_on_off, "OFF") == 0) {
        // correct OFF request, notify new status
        isActive = false;
        LOG_INFO("Switch off\n");

        coap_set_status_code(response, CHANGED_2_04);

        char msg[max_char_len];
        snprintf(msg, max_char_len + 1, "%s", "OFF");  // +1 = end string

        size_t len = strlen(msg);
        memcpy(buffer, (const void *)msg, len);

        coap_set_header_content_format(response, TEXT_PLAIN);
        coap_set_header_etag(response, (uint8_t *)&len, 1);
        coap_set_payload(response, buffer, len);
      }

    } else {
      // incorrect request
      LOG_INFO("Bad Request\n");
      coap_set_status_code(response, BAD_REQUEST_4_00);
    }
}
