import Icon from 'luminor-components/lib/Components/Icon/index';
import FramePage from 'luminor-components/lib/Frames/Page/index';
import * as React from 'react';
import { Trans } from 'react-i18next';
import { useSelector } from 'react-redux';

import { IStoreState } from 'data/rootReducer';
import { CurrentLanguage } from 'utils/helpers';

interface IProps {
    children: any;
    merchantName: string;
    breakpointMobile: boolean;
    onChangeLanguage?: (language: any) => void;
    languagesToShow?: CurrentLanguage[];
}

const Page: React.FC<IProps> = ({
    children,
    merchantName,
    onChangeLanguage,
    breakpointMobile,
    languagesToShow = [],
}) => {
    const languages = languagesToShow.map((availableLanguage: CurrentLanguage) => ({
        label: availableLanguage.toUpperCase(),
        value: availableLanguage,
    }));
    const currentLanguage = useSelector((state: IStoreState) => state.currentLanguageState.currentLanguage);

    const title = (
        <>
            <span>
                <Trans
                    i18nKey="general.youCameFromWebsite"
                    values={{ merchantName }}
                    components={{ bold: <strong /> }}
                />
            </span>
        </>
    );

    const visaLogo = (
        <Icon
            width={breakpointMobile ? 90 : 110}
            height={breakpointMobile ? 60 : 110}
            kind={breakpointMobile ? 'visa' : 'visa-secure'}
        />
    );

    return (
        <div className="frame-main">
            <FramePage
                title={title}
                currentLanguage={languages.find((language) => language.value === currentLanguage)}
                languages={languages.filter((language) => language.value !== currentLanguage)}
                onLanguageSelect={onChangeLanguage}
                addOnRight={visaLogo}
                contentClassName="main"
            >
                {children}
            </FramePage>
        </div>
    );
};

export default Page;
