#include "random.h"
#include "aquifer_parameters.h"
#include "time.h"

/*   The following code is just a simulation of the output of a level sensor   */

/* IDEA : during summer, the sensed level with be probably lower than needed, thus water will be scarse.
On the contrary during rainy seasons the water level will probably be enough to cover the needs*/

/* DEFAULT VALUES*/
/*needed water is expressed in terms of cm^3 accumulated between 2 successive dispensings, those are default values*/
#define NOT_NEEDED 0
#define LOW_NEED 2
#define MEDIUM_NEED 4
#define HIGH_NEED 6
#define VERY_HIGH_NEED 7


static double simulate_level(){
    bool summer = false;
    time_t t = time(NULL);
    struct tm tm = *localtime(&t);
    int month = tm.tm_mon;
    if (month >=5 && month<8)  //between June and August
        summer = true;
    double availability;  //   cm^3
    if (summer)
        availability = rand()%MEDIUM_NEED;
    else
        availability = MEDIUM_NEED + rand()%(VERY_HIGH_NEED - MEDIUM_NEED);

    //Assuming rectangular aquifer, available water is given by LEVEL * SECTION * WATER_SPEED * INTERVAL
    double level = ((availability/WATER_SPEED)/SECTION)/PUBLISH_INTERVAL;   //cm
    return level<MAX_LEVEL ? level : MAX_LEVEL;
}

