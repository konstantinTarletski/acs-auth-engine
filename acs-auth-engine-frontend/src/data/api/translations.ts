/* eslint-disable max-len */
import axios from 'axios';

import apis from 'data/api/apis';

export interface ILanguageSpecificTranslations {} // TODO: fill in interface

export interface ITranslations {
    en?: ILanguageSpecificTranslations;
    ru?: ILanguageSpecificTranslations;
    lt?: ILanguageSpecificTranslations;
    lv?: ILanguageSpecificTranslations;
    ee?: ILanguageSpecificTranslations;
}

/* const mockTranslations = {
    en: {
        screens: {
            failure: {
                backToMerchantButton: 'BACK TO THE MERCHANT',
            },
            auth: {
                code: {
                    title: 'Control code:',
                    description: {
                        SMART_ID:
                            'Login message was sent to your Smart-ID device.\n\n\nPLEASE NOTE: enter the PIN code only after making sure that the control code displayed on your phone matches the code displayed here!',
                        M_SIGNATURE:
                            'Login message was sent to your mobile phone.\n\nPLEASE NOTE: enter the PIN code only after making sure that the control code displayed on your phone matches the code displayed here!',
                    },
                },
                backButton: 'BACK',
            },
            authFailure: {
                buttons: {
                    tryAgain: 'BACK',
                    cancel: 'CANCEL',
                },
            },
            authSuccess: {
                justPaid: 'Payment for <bold>{{sum}}</bold> has been confirmed',
                youWillBeRedirected: "You'll be redirected back to the merchants website after",
                backToMerchantButton: 'BACK TO THE MERCHANT',
            },
            authCancel: {
                title: 'Do you want to cancel your payment?',
                description: 'If you cancel, your payment will not be confirmed',
                buttons: {
                    continue: 'CANCEL PAYMENT',
                    back: 'BACK',
                },
            },
            choose: {
                title: 'Keep Your Account Safe',
                description: 'Please authenticate the payment',
                buttons: {
                    continue: 'CONFIRM',
                    cancel: 'CANCEL',
                },
            },
            authEnterCode: {
                enterResponseCode:
                    'Enter one-time code, which is displayed on the generator screen (after the message “APPLI-“ appears, press 1).',
                confirmationCodeLabel: 'Generator code',
                buttons: {
                    continue: 'CONFIRM',
                    back: 'BACK',
                },
                unexpectedCharacterError: 'Unexpected character',
            },
            internetBankLogin: {
                enterInternetBankLogin: 'Please enter your internet bank login code.',
                loginCodeInputLabel: 'Login code',
                buttons: {
                    continue: 'CONFIRM',
                    cancel: 'CANCEL',
                },
                unexpectedCharacterError: 'Unexpected character',
            },
        },
        paymentDetails: {
            amount: 'AMOUNT',
            card: 'PAYMENT CARD',
            merchant: 'MERCHANT',
        },
        general: {
            youCameFromWebsite: 'You came here from website:',
        },
        acsInternalMessages: {
            smartIdUserMessage: 'Please confirm the payment for %s EUR',
        },
        errors: {
            INTERNAL_SERVICE_IS_NOT_REACHABLE: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            NO_LOGINS_AVAILABLE: {
                title: 'Payment could not be authenticated',
                description: 'No valid Luminor Internet bank account.',
            },
            BLOCKED_LOGIN_CODE: {
                title: 'Payment was not authenticated',
                description:
                    'Your internet bank account is blocked because the login data might have been entered incorrectly. To have your access unblocked, please call 1608.',
            },
            NO_SMART_ID: {
                title: 'Payment could not be authenticated',
                description: 'Chosen authentication method is not linked to your internet bank account.',
            },
            NO_PHONE_NUMBER: {
                title: 'Payment was not authenticated',
                description:
                    'In order to ensure security of your funds, please confirm that mobile phone number used for authentication is correct - log in to your internet bank with M-signature. Repeat this purchase after confirmation.',
            },
            SMART_ID_BLOCKED: {
                title: 'Payment was not authenticated',
                description:
                    'Your Smart-ID account is suspended. Check your phone (Smart-ID application) for more details.',
            },
            SMART_ID_FAILED: {
                title: 'Payment was not authenticated',
                description: 'You have cancelled the purchase authentication.',
            },
            THIS_AUTHENTICATION_TYPE_CANNOT_BE_USED_WITH_THIS_LOGIN: {
                title: 'Payment could not be authenticated',
                description: 'Chosen authentication method is not linked to your Internet bank account.',
            },
            SK74_NO_MID_CERT: {
                title: 'Payment was not authenticated',
                description: 'Mobile phone number has no active signature certificates.',
            },
            SK73_TIMEOUT: {
                title: 'Payment was not authenticated',
                description: 'Please try again: check your phone for more details and follow authentication steps.',
            },
            OTHER_SESSION_END_RESULT_CODE: {
                title: 'Payment was not authenticated.',
                description: 'Re-initiate the purchase or choose another authentication method you are usually using.',
            },
            SK75_MID_CONFIG_ERROR: {
                title: 'Payment was not authenticated',
                description:
                    "M-signature configuration on SIM card differs from what is configured on service provider's side. Please contact your mobile operator. (75)",
            },
            SK76_SIM_NOT_AVAILABLE: {
                title: 'Payment was not authenticated',
                description:
                    'Re-initiate the purchase or choose another authentication method you are usually using. (76)',
            },
            SK77_SMS_SENDING_ERROR: {
                title: 'Payment was not authenticated',
                description:
                    'Re-initiate the purchase or choose another authentication method you are usually using. (77)',
            },
            SK78_SIM_ERROR: {
                title: 'Payment was not authenticated',
                description:
                    'Re-initiate the purchase or choose another authentication method you are usually using. (78)',
            },
            AUTHENTICATION_FAILED: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            SMART_ID_TIMEOUT: {
                title: 'Payment was not authenticated',
                description:
                    'Please try again: check your phone (Smart-ID application) for more details and follow authentication steps.',
            },
            SESSION_HAS_EXPIRED: {
                title: 'Payment was not authenticated',
                description: 'The time for purchase authentication has expired.',
            },
            WRONG_CODE_FOR_CODE_CALCULATOR: {
                title: 'Incorrect code',
                description: 'Incorrect generator code. Please try again.',
            },
            OPERATION_FAILED: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            OPERATION_TERMINATED: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            FATAL_EXCEPTION: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            LOGIN_NOT_ACTIVE: {
                title: 'Payment could not be authenticated',
                description:
                    'No valid Luminor Internet bank account or it is blocked because the login data might have been entered incorrectly.',
            },
            NO_SUPPORTED_AUTH_METHODS_AVAILABLE: {
                title: 'Payment could not be authenticated',
                description: 'You do not have an authentication tool linked to your internet bank account.',
            },
            EXCEEDED_MAX_ATTEMPTS_FOR_AUTH_METHOD_CHANGE: {
                title: 'Payment was not authenticated',
                description:
                    'You have exceeded allowed count of changing the authentication methods. Please re-initiate the purchase.',
            },
            EXCEEDED_MAX_ATTEMPTS_FOR_ENTERING_LOGIN: {
                title: 'Incorrect login code',
                description: 'Make sure you enter correct Internet bank login code and re-initiate the purchase.',
            },
            NO_MATCHING_LOGIN_FOUND: {
                title: 'Incorrect code',
                description: 'Incorrect login code. Please try again',
            },
            NO_ACTIVE_PERSON_ACCOUNTS_FOUND: {
                title: '-',
                description: 'NO_ACTIVE_PERSON_ACCOUNTS_FOUND',
            },
            CARD_IS_BLOCKED: {
                title: 'The payment card is not valid',
                description: 'Check your card status in the internet bank and make sure E-purchase service is enabled.',
            },
            CARD_BLOCKED_CMS: {
                title: 'The payment card is not valid',
                description: 'Check your card status in the internet bank and make sure E-purchase service is enabled.',
            },
            CARD_BLOCKED_RMS: {
                title: 'The payment card is not valid',
                description: 'Check your card status in the internet bank and make sure E-purchase service is enabled.',
            },
            CARD_BLOCKED_ECOMMERCE: {
                title: 'The payment card is not valid',
                description: 'Check your card status in the internet bank and make sure E-purchase service is enabled.',
            },
            GENERAL_EXCEPTION: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            JSON_BODY_VALIDATION_EXCEPTION: {
                title: 'Payment was not authenticated',
                description: 'Please re-initiate the purchase.',
            },
            AUTHENTICATION_TYPE_NOT_ALLOWED: {
                title: '-',
                description: 'AUTHENTICATION_TYPE_NOT_ALLOWED',
            },
            WRONG_APPLICATION_STATE: {
                title: '-',
                description: 'WRONG_APPLICATION_STATE',
            },
            SESSION_DOES_NOT_EXIST: {
                title: 'Payment was not authenticated',
                description: 'Please try again.',
            },
        },
    },
    lt: {
        screens: {
            failure: {
                backToMerchantButton: 'GRĮŽTI PAS PARDAVĖJĄ',
            },
            auth: {
                code: {
                    title: 'Kontrolinis kodas:',
                    description: {
                        SMART_ID:
                            'Į jūsų Smart-ID programėlę išsiųstas prisijungimo pranešimas.\n\nDĖMESIO: įveskite PIN kodą tik įsitikinę, kad telefone rodomas kontrolinis kodas sutampa su esančiu čia!',
                        M_SIGNATURE:
                            'Į Jūsų mobilųjį telefoną išsiųstas prisijungimo pranešimas.\n\n\nDĖMESIO: įveskite PIN kodą tik įsitikinę, kad telefone rodomas kontrolinis kodas sutampa su esančiu čia!',
                    },
                },
                backButton: 'GRĮŽTI',
            },
            authFailure: {
                buttons: {
                    tryAgain: 'GRĮŽTI',
                    cancel: 'ATŠAUKTI',
                },
            },
            authSuccess: {
                justPaid: 'Mokėjimas <bold>{{sum}}</bold> sumai buvo patvirtintas',
                youWillBeRedirected: 'Būsite nukreiptas atgal į pardavėjo svetainę po',
                backToMerchantButton: 'GRĮŽTI PAS PARDAVĖJĄ',
            },
            authCancel: {
                title: 'Ar Jūs tikrai norite atšaukti mokėjimą?',
                description: 'Jeigu atšauksite, jūsų mokėjimas nebus patvirtintas.',
                buttons: {
                    continue: 'ATŠAUKTI MOKĖJIMĄ',
                    back: 'GRĮŽTI',
                },
            },
            choose: {
                title: 'Apsaugokite savo lėšas sąskaitoje',
                description: 'Patvirtinkite mokėjimą',
                buttons: {
                    continue: 'PATVIRTINTI',
                    cancel: 'ATŠAUKTI',
                },
            },
            authEnterCode: {
                enterResponseCode:
                    'Įveskite vienkartinį kodą, kurį matote generatoriaus ekrane (generatoriaus ekrane pasirodžius „APPLI-“, spauskite „1).',
                confirmationCodeLabel: 'Generatoriaus kodas',
                buttons: {
                    continue: 'PATVIRTINTI',
                    back: 'GRĮŽTI',
                },
                unexpectedCharacterError: 'Netinkamas simbolis',
            },
            internetBankLogin: {
                enterInternetBankLogin: 'Įveskite savo interneto banko prisijungimo kodą.',
                loginCodeInputLabel: 'Prisijungimo kodas',
                buttons: {
                    continue: 'PATVIRTINTI',
                    cancel: 'ATŠAUKTI',
                },
                unexpectedCharacterError: 'Netinkamas simbolis',
            },
        },
        paymentDetails: {
            amount: 'SUMA',
            card: 'MOKĖJIMO KORTELĖ',
            merchant: 'PARDAVĖJAS',
        },
        general: {
            youCameFromWebsite: 'Atėjote čia iš svetainės:',
        },
        acsInternalMessages: {
            smartIdUserMessage: 'Please confirm the payment for %s EUR',
        },
        errors: {
            INTERNAL_SERVICE_IS_NOT_REACHABLE: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            NO_LOGINS_AVAILABLE: {
                title: 'Mokėjimas negali būti patvirtintas',
                description: 'Nėra galiojančios Luminor interneto banko paskyros.',
            },
            BLOCKED_LOGIN_CODE: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Jūsų interneto banko paskyra blokuojama, nes prisijungimo duomenys galėjo būti įvesti klaidingai. Prisijungimo atblokavimui prašome kreiptis telefonu 1608.',
            },
            NO_SMART_ID: {
                title: 'Mokėjimas negali būti patvirtintas',
                description:
                    'Pasirinkta autentiškumą patvirtinanti priemonė nėra susieta su jūsų interneto banko paskyra.',
            },
            NO_PHONE_NUMBER: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Siekdami užtikrinti jūsų lėšų saugumą, prašome patvirtinti, kad autentiškumo patvirtinime naudojamas mobiliojo telefono numeris yra teisingas - prisijunkite prie interneto banko su M. parašu, o tuomet pakartokite šį mokėjimą.',
            },
            SMART_ID_BLOCKED: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Jūsų Smart-ID paskyra užblokuota. Patikrinkite informaciją telefone (Smart-ID programėlėje).',
            },
            SMART_ID_FAILED: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Atšaukėte mokėjimo patvirtinimą.',
            },
            THIS_AUTHENTICATION_TYPE_CANNOT_BE_USED_WITH_THIS_LOGIN: {
                title: 'Mokėjimas negali būti patvirtintas',
                description:
                    'Pasirinkta autentiškumą patvirtinanti priemonė nėra susieta su jūsų interneto banko paskyra.',
            },
            SK74_NO_MID_CERT: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Naudojamas mobiliojo telefono numeris neturi galiojančio elektroninio parašo sertifikato.',
            },
            SK73_TIMEOUT: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite dar kartą: patikrinkite informaciją ir sekite nurodymus telefone.',
            },
            OTHER_SESSION_END_RESULT_CODE: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Pakartokite mokėjimą dar kartą arba pasirinkite kitą jūsų naudojamą autentiškumą patvirtinačią priemonę.',
            },
            SK75_MID_CONFIG_ERROR: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'SIM kortelės nustatymai M. parašo aplikacijoje nesutampa su paslaugos teikėjo nustatymais. Prašome kreiptis į savo mobilaus ryšio operatorių. (75)',
            },
            SK76_SIM_NOT_AVAILABLE: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Pakartokite mokėjimą dar kartą arba pasirinkite kitą jūsų naudojamą autentiškumą patvirtinačią priemonę. (76)',
            },
            SK77_SMS_SENDING_ERROR: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Pakartokite mokėjimą dar kartą arba pasirinkite kitą jūsų naudojamą autentiškumą patvirtinačią priemonę. (77)',
            },
            SK78_SIM_ERROR: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Pakartokite mokėjimą dar kartą arba pasirinkite kitą jūsų naudojamą autentiškumą patvirtinačią priemonę. (78)',
            },
            AUTHENTICATION_FAILED: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            SMART_ID_TIMEOUT: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Pakartokite dar kartą: patikrinkite informaciją ir sekite nurodymus telefone (Smart-ID programėlėje).',
            },
            SESSION_HAS_EXPIRED: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pasibaigė mokėjimo patvirtinimui skirtas laikas.',
            },
            WRONG_CODE_FOR_CODE_CALCULATOR: {
                title: 'Neteisingas kodas',
                description: 'Neteisingas generatoriaus kodas. Pakartokite dar kartą.',
            },
            OPERATION_FAILED: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            OPERATION_TERMINATED: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            FATAL_EXCEPTION: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            LOGIN_NOT_ACTIVE: {
                title: 'Mokėjimas negali būti patvirtintas',
                description:
                    'Nėra galiojančios Luminor interneto banko paskyros arba ji yra blokuojama, nes prisijungimo duomenys galėjo būti įvesti klaidingai.',
            },
            NO_SUPPORTED_AUTH_METHODS_AVAILABLE: {
                title: 'Mokėjimas negali būti patvirtintas',
                description:
                    'Neturite su jūsų interneto banko paskyra susietos autentiškumą patvirtinančios priemonės.',
            },
            EXCEEDED_MAX_ATTEMPTS_FOR_AUTH_METHOD_CHANGE: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description:
                    'Viršijote leistiną autentiškumą patvirtinančių priemonių pasirinkimo skaičių. Pakartokite mokėjimą dar kartą.',
            },
            EXCEEDED_MAX_ATTEMPTS_FOR_ENTERING_LOGIN: {
                title: 'Neteisingas prisijungimo kodas',
                description:
                    'Įsitikinkite, kad vedate teisingą savo interneto banko prisijungimo kodą ir pakartokite mokėjimą dar kartą.',
            },
            NO_MATCHING_LOGIN_FOUND: {
                title: 'Neteisingas kodas',
                description: 'Neteisingas prisijungimo kodas. Pakartokite dar kartą.',
            },
            NO_ACTIVE_PERSON_ACCOUNTS_FOUND: {
                title: '-',
                description: 'NO_ACTIVE_PERSON_ACCOUNTS_FOUND',
            },
            CARD_IS_BLOCKED: {
                title: 'Negaliojanti mokėjimo kortelė',
                description:
                    'Patikrinkite savo kortelės būklę interneto banke ir įsitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            CARD_BLOCKED_CMS: {
                title: 'Negaliojanti mokėjimo kortelė',
                description:
                    'Patikrinkite savo kortelės būklę interneto banke ir įsitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            CARD_BLOCKED_RMS: {
                title: 'Negaliojanti mokėjimo kortelė',
                description:
                    'Patikrinkite savo kortelės būklę interneto banke ir įsitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            CARD_BLOCKED_ECOMMERCE: {
                title: 'Negaliojanti mokėjimo kortelė',
                description:
                    'Patikrinkite savo kortelės būklę interneto banke ir įsitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            GENERAL_EXCEPTION: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            JSON_BODY_VALIDATION_EXCEPTION: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite mokėjimą dar kartą.',
            },
            AUTHENTICATION_TYPE_NOT_ALLOWED: {
                title: '-',
                description: 'AUTHENTICATION_TYPE_NOT_ALLOWED',
            },
            WRONG_APPLICATION_STATE: {
                title: '-',
                description: 'WRONG_APPLICATION_STATE',
            },
            SESSION_DOES_NOT_EXIST: {
                title: 'Mokėjimas nebuvo patvirtintas',
                description: 'Pakartokite dar kartą.',
            },
        },
    },
}; */

const getTranslations = async () => {
    const { data } = await axios.get<ITranslations>(apis.GET_TRANSLATIONS);

    return data;
};

export default getTranslations;
