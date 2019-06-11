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
        return nullptr;
    }

    EC_KEY *key = nullptr;
    key = PEM_read_bio_ECPrivateKey(originalPemBio, &key, nullptr, userPassword);
    if (!key) {
        return nullptr;
    }
    int result = BIO_free(originalPemBio);
    if (!result) {
        return nullptr;
    }

    EC_KEY_set_asn1_flag(key, OPENSSL_EC_NAMED_CURVE);

    BIO *derBio = BIO_new(BIO_s_mem());
    result = i2d_ECPrivateKey_bio(derBio, key);
    if (!result) {
        return nullptr;
    }

    char *derBuffer;
    int readSize = (int) BIO_get_mem_data(derBio, &derBuffer);

    char *hex = OPENSSL_buf2hexstr(reinterpret_cast<const unsigned char *>(derBuffer), readSize);

    BIO_free(derBio);
    EC_KEY_free(key);

    return env->NewStringUTF(hex);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_org_metahash_metawallet_extensions_PrivateWalletHelper_encryptPrivateKey(
        JNIEnv *env,
        jobject /* this */,
        jbyteArray privateKey,
        jstring userPass
) {

    //key
    int keyLength = env->GetArrayLength(privateKey);
    auto *sourceData = reinterpret_cast<const unsigned char *>(as_unsigned_char_array(env,
                                                                                      privateKey));

    //password
    auto *userPassword = (unsigned char *) env->GetStringUTFChars(userPass, JNI_FALSE);

    EC_KEY *key = nullptr;

    key = d2i_ECPrivateKey(&key, &sourceData, keyLength);
    if (!key) {
        return nullptr;
    }

    EC_KEY_set_asn1_flag(key, OPENSSL_EC_NAMED_CURVE);

    const EVP_CIPHER *cipher = EVP_aes_128_cbc();

    BIO *privateBio = BIO_new(BIO_s_mem());

    int result = PEM_write_bio_ECPrivateKey(privateBio, key, cipher, nullptr, 0, nullptr,
                                            userPassword);

    if (!result) {
        return nullptr;
    }

    char *tempBuffer;
    BIO_get_mem_data(privateBio, &tempBuffer);
    jstring resultString = env->NewStringUTF(tempBuffer);

    //clear ref
    env->DeleteLocalRef(privateKey);
    env->DeleteLocalRef(userPass);
    BIO_free(privateBio);
    EC_KEY_free(key);

    return resultString;
}