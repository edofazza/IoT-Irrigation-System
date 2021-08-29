#include "random.h"
#include "reservoir_parameters.h"
//#include "time.h"

/*   The following code is just a simulation of the output of a level sensor   */

/* IDEA : during summer, the sensed level with be probably lower than needed, thus water will be scarse.
On the contrary during rainy seasons the water level will probably be enough to cover the needs*/

/* DEFAULT VALUES*/
/*needed water is expressed in terms of cm^3 accumulated between 2 successive dispensings, those are default values*/
#define NOT_NEEDED 0
#define LOW_NEED 20
#define MEDIUM_NEED 40
#define HIGH_NEED 60
#define VERY_HIGH_NEED 70


static int simulate_level(){
    bool summer = false;
    //time_t t = time(NULL);
    //struct tm tm = *localtime(&t);
    //int month = tm.tm_mon;
    int month=8;
    if (month >=5 && month<8)  //between June and August
        summer = true;
    //srand(time(NULL));
    int level;  //   cm
    if (summer)
        level = rand()%MEDIUM_NEED;
    else
        level = MEDIUM_NEED + rand()%(VERY_HIGH_NEED - MEDIUM_NEED);
    return level<MAX_LEVEL ? level : MAX_LEVEL;
}
