import React, { useState } from 'react';
import { AppButton } from './AppButton';
import { ConfirmModal } from './ConfirmModal';

type Props = { onDelete: () => void; loading?: boolean };

export const DeleteButton = ({ onDelete, loading }: Props) => {
  const [visible, setVisible] = useState(false);
  return (
    <>
      <AppButton title="Delete" variant="danger" onPress={() => setVisible(true)} loading={loading} />
      <ConfirmModal visible={visible} title="Delete gym?" message="Are you sure you want to delete?" onCancel={() => setVisible(false)} onConfirm={() => { setVisible(false); onDelete(); }} loading={loading} />
    </>
  );
};
