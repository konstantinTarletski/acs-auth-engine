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
                    'Enter one-time code, which is displayed on the generator screen (after the message ???APPLI-??? appears, press 1).',
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
                backToMerchantButton: 'GR????TI PAS PARDAV??J??',
            },
            auth: {
                code: {
                    title: 'Kontrolinis kodas:',
                    description: {
                        SMART_ID:
                            '?? j??s?? Smart-ID program??l?? i??si??stas prisijungimo prane??imas.\n\nD??MESIO: ??veskite PIN kod?? tik ??sitikin??, kad telefone rodomas kontrolinis kodas sutampa su esan??iu ??ia!',
                        M_SIGNATURE:
                            '?? J??s?? mobil??j?? telefon?? i??si??stas prisijungimo prane??imas.\n\n\nD??MESIO: ??veskite PIN kod?? tik ??sitikin??, kad telefone rodomas kontrolinis kodas sutampa su esan??iu ??ia!',
                    },
                },
                backButton: 'GR????TI',
            },
            authFailure: {
                buttons: {
                    tryAgain: 'GR????TI',
                    cancel: 'AT??AUKTI',
                },
            },
            authSuccess: {
                justPaid: 'Mok??jimas <bold>{{sum}}</bold> sumai buvo patvirtintas',
                youWillBeRedirected: 'B??site nukreiptas atgal ?? pardav??jo svetain?? po',
                backToMerchantButton: 'GR????TI PAS PARDAV??J??',
            },
            authCancel: {
                title: 'Ar J??s tikrai norite at??aukti mok??jim???',
                description: 'Jeigu at??auksite, j??s?? mok??jimas nebus patvirtintas.',
                buttons: {
                    continue: 'AT??AUKTI MOK??JIM??',
                    back: 'GR????TI',
                },
            },
            choose: {
                title: 'Apsaugokite savo l????as s??skaitoje',
                description: 'Patvirtinkite mok??jim??',
                buttons: {
                    continue: 'PATVIRTINTI',
                    cancel: 'AT??AUKTI',
                },
            },
            authEnterCode: {
                enterResponseCode:
                    '??veskite vienkartin?? kod??, kur?? matote generatoriaus ekrane (generatoriaus ekrane pasirod??ius ???APPLI-???, spauskite ???1).',
                confirmationCodeLabel: 'Generatoriaus kodas',
                buttons: {
                    continue: 'PATVIRTINTI',
                    back: 'GR????TI',
                },
                unexpectedCharacterError: 'Netinkamas simbolis',
            },
            internetBankLogin: {
                enterInternetBankLogin: '??veskite savo interneto banko prisijungimo kod??.',
                loginCodeInputLabel: 'Prisijungimo kodas',
                buttons: {
                    continue: 'PATVIRTINTI',
                    cancel: 'AT??AUKTI',
                },
                unexpectedCharacterError: 'Netinkamas simbolis',
            },
        },
        paymentDetails: {
            amount: 'SUMA',
            card: 'MOK??JIMO KORTEL??',
            merchant: 'PARDAV??JAS',
        },
        general: {
            youCameFromWebsite: 'At??jote ??ia i?? svetain??s:',
        },
        acsInternalMessages: {
            smartIdUserMessage: 'Please confirm the payment for %s EUR',
        },
        errors: {
            INTERNAL_SERVICE_IS_NOT_REACHABLE: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
            },
            NO_LOGINS_AVAILABLE: {
                title: 'Mok??jimas negali b??ti patvirtintas',
                description: 'N??ra galiojan??ios Luminor interneto banko paskyros.',
            },
            BLOCKED_LOGIN_CODE: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'J??s?? interneto banko paskyra blokuojama, nes prisijungimo duomenys gal??jo b??ti ??vesti klaidingai. Prisijungimo atblokavimui pra??ome kreiptis telefonu 1608.',
            },
            NO_SMART_ID: {
                title: 'Mok??jimas negali b??ti patvirtintas',
                description:
                    'Pasirinkta autenti??kum?? patvirtinanti priemon?? n??ra susieta su j??s?? interneto banko paskyra.',
            },
            NO_PHONE_NUMBER: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Siekdami u??tikrinti j??s?? l?????? saugum??, pra??ome patvirtinti, kad autenti??kumo patvirtinime naudojamas mobiliojo telefono numeris yra teisingas - prisijunkite prie interneto banko su M. para??u, o tuomet pakartokite ???? mok??jim??.',
            },
            SMART_ID_BLOCKED: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'J??s?? Smart-ID paskyra u??blokuota. Patikrinkite informacij?? telefone (Smart-ID program??l??je).',
            },
            SMART_ID_FAILED: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'At??auk??te mok??jimo patvirtinim??.',
            },
            THIS_AUTHENTICATION_TYPE_CANNOT_BE_USED_WITH_THIS_LOGIN: {
                title: 'Mok??jimas negali b??ti patvirtintas',
                description:
                    'Pasirinkta autenti??kum?? patvirtinanti priemon?? n??ra susieta su j??s?? interneto banko paskyra.',
            },
            SK74_NO_MID_CERT: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Naudojamas mobiliojo telefono numeris neturi galiojan??io elektroninio para??o sertifikato.',
            },
            SK73_TIMEOUT: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite dar kart??: patikrinkite informacij?? ir sekite nurodymus telefone.',
            },
            OTHER_SESSION_END_RESULT_CODE: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Pakartokite mok??jim?? dar kart?? arba pasirinkite kit?? j??s?? naudojam?? autenti??kum?? patvirtina??i?? priemon??.',
            },
            SK75_MID_CONFIG_ERROR: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'SIM kortel??s nustatymai M. para??o aplikacijoje nesutampa su paslaugos teik??jo nustatymais. Pra??ome kreiptis ?? savo mobilaus ry??io operatori??. (75)',
            },
            SK76_SIM_NOT_AVAILABLE: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Pakartokite mok??jim?? dar kart?? arba pasirinkite kit?? j??s?? naudojam?? autenti??kum?? patvirtina??i?? priemon??. (76)',
            },
            SK77_SMS_SENDING_ERROR: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Pakartokite mok??jim?? dar kart?? arba pasirinkite kit?? j??s?? naudojam?? autenti??kum?? patvirtina??i?? priemon??. (77)',
            },
            SK78_SIM_ERROR: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Pakartokite mok??jim?? dar kart?? arba pasirinkite kit?? j??s?? naudojam?? autenti??kum?? patvirtina??i?? priemon??. (78)',
            },
            AUTHENTICATION_FAILED: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
            },
            SMART_ID_TIMEOUT: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Pakartokite dar kart??: patikrinkite informacij?? ir sekite nurodymus telefone (Smart-ID program??l??je).',
            },
            SESSION_HAS_EXPIRED: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pasibaig?? mok??jimo patvirtinimui skirtas laikas.',
            },
            WRONG_CODE_FOR_CODE_CALCULATOR: {
                title: 'Neteisingas kodas',
                description: 'Neteisingas generatoriaus kodas. Pakartokite dar kart??.',
            },
            OPERATION_FAILED: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
            },
            OPERATION_TERMINATED: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
            },
            FATAL_EXCEPTION: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
            },
            LOGIN_NOT_ACTIVE: {
                title: 'Mok??jimas negali b??ti patvirtintas',
                description:
                    'N??ra galiojan??ios Luminor interneto banko paskyros arba ji yra blokuojama, nes prisijungimo duomenys gal??jo b??ti ??vesti klaidingai.',
            },
            NO_SUPPORTED_AUTH_METHODS_AVAILABLE: {
                title: 'Mok??jimas negali b??ti patvirtintas',
                description:
                    'Neturite su j??s?? interneto banko paskyra susietos autenti??kum?? patvirtinan??ios priemon??s.',
            },
            EXCEEDED_MAX_ATTEMPTS_FOR_AUTH_METHOD_CHANGE: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description:
                    'Vir??ijote leistin?? autenti??kum?? patvirtinan??i?? priemoni?? pasirinkimo skai??i??. Pakartokite mok??jim?? dar kart??.',
            },
            EXCEEDED_MAX_ATTEMPTS_FOR_ENTERING_LOGIN: {
                title: 'Neteisingas prisijungimo kodas',
                description:
                    '??sitikinkite, kad vedate teising?? savo interneto banko prisijungimo kod?? ir pakartokite mok??jim?? dar kart??.',
            },
            NO_MATCHING_LOGIN_FOUND: {
                title: 'Neteisingas kodas',
                description: 'Neteisingas prisijungimo kodas. Pakartokite dar kart??.',
            },
            NO_ACTIVE_PERSON_ACCOUNTS_FOUND: {
                title: '-',
                description: 'NO_ACTIVE_PERSON_ACCOUNTS_FOUND',
            },
            CARD_IS_BLOCKED: {
                title: 'Negaliojanti mok??jimo kortel??',
                description:
                    'Patikrinkite savo kortel??s b??kl?? interneto banke ir ??sitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            CARD_BLOCKED_CMS: {
                title: 'Negaliojanti mok??jimo kortel??',
                description:
                    'Patikrinkite savo kortel??s b??kl?? interneto banke ir ??sitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            CARD_BLOCKED_RMS: {
                title: 'Negaliojanti mok??jimo kortel??',
                description:
                    'Patikrinkite savo kortel??s b??kl?? interneto banke ir ??sitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            CARD_BLOCKED_ECOMMERCE: {
                title: 'Negaliojanti mok??jimo kortel??',
                description:
                    'Patikrinkite savo kortel??s b??kl?? interneto banke ir ??sitikinkite, kad aktyvuota Saugaus atsiskaitymo internete paslauga.',
            },
            GENERAL_EXCEPTION: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
            },
            JSON_BODY_VALIDATION_EXCEPTION: {
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite mok??jim?? dar kart??.',
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
                title: 'Mok??jimas nebuvo patvirtintas',
                description: 'Pakartokite dar kart??.',
            },
        },
    },
}; */

const getTranslations = async () => {
    const { data } = await axios.get<ITranslations>(apis.GET_TRANSLATIONS);

    return data;
};

export default getTranslations;
