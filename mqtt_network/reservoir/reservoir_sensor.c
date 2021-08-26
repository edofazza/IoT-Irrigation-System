#include "random.h"
#include "reservoir_parameters.h"

/*   The following code is just a simulation of the output of a level sensor and the corresponding actuator that
     puts or fetches the water from the reservoir*/

static double sensed_level=MAX_LEVEL;

static double simulate_level(){
    return sensed_level;
}

static void put_get_water(double quantity){
//assuming rectangular reservoir
    sensed_level += (quantity/WIDTH)/DEPTH;
    if (sensed_level>MAX_LEVEL)
        sensed_level = MAX_LEVEL;
    else if (sensed_level<0)
        sensed_level = 0;
}

