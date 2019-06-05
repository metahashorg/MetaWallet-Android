//
// Created by Artem on 24.04.2018.
//

#include "KeyGenerator.h"

bool generateECKeyFromPrivate(EC_KEY *key, const unsigned char *privBytes, int priveBytesLength) {
    //private bignum part
    BIGNUM *privPart = BN_new();
    BN_bin2bn(privBytes, priveBytesLength, privPart);
    bool success = false;

    BN_CTX *ctx = NULL;
    EC_POINT *pub_key = NULL;
    const EC_GROUP *group = EC_KEY_get0_group(key);

    if ((ctx = BN_CTX_new())) {
        if ((pub_key = EC_POINT_new(group))) {
            if (EC_POINT_mul(group, pub_key, privPart, NULL, NULL, ctx)) {
                if (EC_KEY_set_private_key(key, privPart)) {
                    if (EC_KEY_set_public_key(key, pub_key)) {
                        success = true;
                    }
                }
            }
        }
    }

    if (pub_key) EC_POINT_free(pub_key);
    if (ctx) BN_CTX_free(ctx);
    return success;
}
