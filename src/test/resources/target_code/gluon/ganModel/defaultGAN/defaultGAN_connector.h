#ifndef DEFAULTGAN_CONNECTOR
#define DEFAULTGAN_CONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "defaultGAN_connector_predictor.h"
using namespace arma;
class defaultGAN_connector{
public:
colvec noise;
cube res;
defaultGAN_connector_predictor predictor;
void init()
{
noise=colvec(100);
res = cube(1, 64, 64);
predictor.init();
}
void execute()
{
predictor.noise = noise;
predictor.execute();
res = predictor.data;
}

};
#endif
