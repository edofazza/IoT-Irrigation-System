//assuming rectangular reservoirs of capacity 1000 litres
#define WIDTH 200             //2m = 200cm
#define DEPTH 100               //1m = 100cm
#define MAX_LEVEL 50           //50 cm
//Total capacity is 200*100*50 = 1e6 cm^3 = 1000 l

#define DEFAULT_PUBLISH_INTERVAL    (CLOCK_SECOND>>1)
static long PUBLISH_INTERVAL = DEFAULT_PUBLISH_INTERVAL;