//
// Created by Artem on 24.04.2018.
//


#include <openssl/ec.h>

bool generateECKeyFromPrivate(EC_KEY* key, const unsigned char* privBytes, int priveBytesLength);