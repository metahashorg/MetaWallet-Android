#include <jni.h>
#include <string>
#include "openssl/evp.h"
#include "openssl/ec.h"
#include "openssl/bio.h"
#include "openssl/pem.h"
#include "KeyGenerator.h"

unsigned char *as_unsigned_char_array(JNIEnv *env, jbyteArray array) {
    int len = env->GetArrayLength(array);
    auto *buf = new unsigned char[len];
    env->GetByteArrayRegion(array, 0, len, reinterpret_cast<jbyte *>(buf));
    env->DeleteLocalRef(array);
    return buf;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_metahash_metawallet_extensions_PrivateWalletHelper_decryptPrivateKey(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray encryptedPEMInfo,
        jstring userPass
) {

    //pem info
    auto *sourceData = reinterpret_cast<const char *>(as_unsigned_char_array(env,
                                                                             encryptedPEMInfo));

    //password
    auto *userPassword = (unsigned char *) env->GetStringUTFChars(userPass, JNI_FALSE);

    //bio
    BIO *originalPemBio = BIO_new(BIO_s_mem());
    int len = BIO_write(originalPemBio, sourceData, strlen(sourceData));
    if (len <= 0) {
        return 0;
    }

    EC_KEY *key = nullptr;
    key = PEM_read_bio_ECPrivateKey(originalPemBio, &key, nullptr, userPassword);
    if (!key) {
        return 0;
    }
    int result = BIO_free(originalPemBio);
    if (!result) {
        return 0;
    }

    EC_KEY_set_asn1_flag(key, OPENSSL_EC_NAMED_CURVE);

    BIO *derBio = BIO_new(BIO_s_mem());
    result = i2d_ECPrivateKey_bio(derBio, key);
    if (!result) {
        return 0;
    }

    char *derBuffer;
    int readSize = (int) BIO_get_mem_data(derBio, &derBuffer);

    char *hex = OPENSSL_buf2hexstr(reinterpret_cast<const unsigned char *>(derBuffer), readSize);

    BIO_free(derBio);
    EC_KEY_free(key);

    return env->NewStringUTF(hex);
}

/*JNIEXPORT jstring JNICALL
Java_ru_wearemad_newwallettest_MainActivity_stringFromMD5(
        JNIEnv *env,
        jobject thiz,
        jstring srcjStr) {

    const char *unicodeChar = env->GetStringUTFChars(srcjStr, NULL);
    size_t unicodeCharLength = env->GetStringLength(srcjStr);


    unsigned char md[MD5_DIGEST_LENGTH];
    int i;
    char buf[33] = {'\0'};
    MD5((unsigned char*)unicodeChar, unicodeCharLength, (unsigned char*)&md);
    for (i = 0; i < 16; i++) {
        sprintf(&buf[i*2], "%02x", md[i]);
    }
    env->ReleaseStringUTFChars(srcjStr, unicodeChar);
    return env->NewStringUTF(buf);
}*/
