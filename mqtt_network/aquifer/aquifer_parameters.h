#define WATER_SPEED 0.0005    /* 0.0005cm/s   https://www.arpa.vda.it/it/acqua/acque-sotterranee/cosa-sono-le-acque-sotterranee */
#define SECTION 200             //2m
#define MAX_LEVEL 60           //60cm

#define DEFAULT_PUBLISH_INTERVAL    (CLOCK_SECOND*10)
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static long PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;