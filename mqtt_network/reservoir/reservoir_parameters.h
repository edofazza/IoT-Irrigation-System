//assuming rectangular reservoirs of capacity 1000 litres
#define WIDTH 200             //2m = 200cm
#define DEPTH 100               //1m = 100cm
#define RES_MAX_LEVEL 50           //50 cm
//Total capacity is 200*100*50 = 1e6 cm^3 = 1000 l

static int sensed_level = RES_MAX_LEVEL>>1;
static int capacity = (RES_MAX_LEVEL>>1)*WIDTH*DEPTH;

#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)
#define DEFAULT_STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static long PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;
static long STATE_MACHINE_PERIODIC = DEFAULT_STATE_MACHINE_PERIODIC;