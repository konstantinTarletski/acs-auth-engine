import ObjectDetails from 'luminor-components/lib/Components/ObjectDetails/index';
import Well from 'luminor-components/lib/Components/Well/index';
import * as React from 'react';
import { useTranslation } from 'react-i18next';

interface IProps {
    amount: string;
    currency: string;
    paymentCard: string;
    title: string;
    description: string;
    merchantName?: string;
}

const PaymentDetails: React.FC<IProps> = ({ amount, currency, paymentCard, title, description, merchantName }) => {
    const { t } = useTranslation();

    return (
        <>
            <div className="main-title">{title}</div>
            <div className="main-description">{description}</div>
            <Well>
                <ObjectDetails
                    layout="centered"
                    data={[
                        { label: t('paymentDetails.amount'), value: `${amount} ${currency}` },
                        ...(merchantName ? [{ label: t('paymentDetails.merchant'), value: `${merchantName}` }] : []),
                        { label: t('paymentDetails.card'), value: `${paymentCard}` },
                    ]}
                />
            </Well>
        </>
    );
};

export default PaymentDetails;
