import React from 'react';
import { Modal, View, Text, StyleSheet } from 'react-native';
import { AppButton } from './AppButton';

type Props = { visible: boolean; title: string; message?: string; onConfirm: () => void; onCancel: () => void; loading?: boolean };

export const ConfirmModal = ({ visible, title, message, onConfirm, onCancel, loading }: Props) => (
  <Modal visible={visible} transparent animationType="fade">
    <View style={styles.overlay}>
      <View style={styles.box}>
        <Text style={styles.title}>{title}</Text>
        {message ? <Text style={styles.msg}>{message}</Text> : null}
        <View style={styles.row}>
          <AppButton title="Cancel" variant="outline" onPress={onCancel} style={{ flex: 1, marginRight: 6 } as any} />
          <AppButton title="Confirm" variant="danger" onPress={onConfirm} loading={loading} style={{ flex: 1, marginLeft: 6 } as any} />
        </View>
      </View>
    </View>
  </Modal>
);

const styles = StyleSheet.create({
  overlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', justifyContent: 'center', padding: 20 },
  box: { backgroundColor: '#fff', borderRadius: 10, padding: 16 },
  title: { fontSize: 16, fontWeight: '700', marginBottom: 8 },
  msg: { color: '#555', marginBottom: 12 },
  row: { flexDirection: 'row' },
});
