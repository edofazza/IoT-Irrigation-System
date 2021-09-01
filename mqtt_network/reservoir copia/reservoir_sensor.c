#include "reservoir_parameters.h"

/*   The following code is just a simulation of the output of a level sensor and the corresponding actuator that
     puts or fetches the water from the reservoir*/

static int simulate_level(){
    return sensed_level;
}

static int get_capacity(){
    return capacity;
}

static void put_get_water(int quantity){
//assuming rectangular reservoir
    capacity += quantity;
    sensed_level = capacity/WIDTH/DEPTH;
    if (sensed_level>RES_MAX_LEVEL){
        sensed_level = RES_MAX_LEVEL;
    }
    else if (sensed_level<0){
        sensed_level = 0;
    }
    if (capacity>RES_MAX_LEVEL*WIDTH*DEPTH){
        capacity = RES_MAX_LEVEL*WIDTH*DEPTH;
    }
    else if (capacity<0){
        capacity = 0;
    }
}