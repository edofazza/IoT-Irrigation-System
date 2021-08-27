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


