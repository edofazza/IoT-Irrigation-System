//assuming rectangular reservoirs of capacity 1000 litres
//#define RES_WIDTH 200             //2m = 200cm
//#define RES_DEPTH 100               //1m = 100cm
#define MAX_LEVEL 50           //50 cm
#define WATER_SPEED 0.0005    /* 0.0005cm/s   https://www.arpa.vda.it/it/acqua/acque-sotterranee/cosa-sono-le-acque-sotterranee */
#define SECTION 200             //2m
//Total capacity is 200*100*50 = 1e6 cm^3 = 1000 l

#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define DEFAULT_STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static long PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;
//static long STATE_MACHINE_PERIODIC = DEFAULT_STATE_MACHINE_PERIODIC;