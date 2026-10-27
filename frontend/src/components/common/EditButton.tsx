import React from 'react';
import { AppButton } from './AppButton';

export const EditButton = ({ onPress }: { onPress: () => void }) => (
  <AppButton title="Edit" variant="outline" onPress={onPress} />
);
