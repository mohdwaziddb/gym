import React, { useCallback, useState } from 'react';
import { Alert } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { BrutalEmpty } from '../components/brutal/BrutalEmpty';
import { Skeleton } from '../components/brutal/Skeleton';
import { BottomBar } from '../components/brutal/BottomBar';
import { ConfirmModal } from '../components/common/ConfirmModal';
import { GymCard } from '../components/gym/GymCard';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { fetchGyms, deleteGym } from '../store/gymSlice';
import { stringValue } from '../utils/typeUtils';

/** Gyms (gym_details) list - BottomBar ka 04 tab */
export const HomeScreen = ({ navigation }: any) => {
  const dispatch = useAppDispatch();
  const { list, loading, error } = useAppSelector((s) => s.gym);
  const [toDelete, setToDelete] = useState<any | null>(null);

  // Screen pe wapas aate hi (Add/Edit ke baad bhi) list fresh load ho
  useFocusEffect(
    useCallback(() => {
      dispatch(fetchGyms());
    }, [dispatch]),
  );

  const onDelete = async () => {
    if (!toDelete) return;
    const res: any = await dispatch(deleteGym(toDelete.id));
    setToDelete(null);
    if (res.type?.endsWith('/rejected')) Alert.alert('Failed', stringValue(res.payload));
  };

  return (
    <BrutalScreen
      refreshing={loading && list.length > 0}
      onRefresh={() => dispatch(fetchGyms())}
      footer={<BottomBar active="Home" navigation={navigation} />}
    >
      <BrutalHeader
        section="04 / GYMS"
        title="Gym profile"
        subtitle={`${list.length} ${list.length === 1 ? 'gym' : 'gyms'}`}
        right={<BrutalButton title="+ Add" small onPress={() => navigation.navigate('GymForm')} />}
      />

      {loading && list.length === 0 ? <Skeleton rows={2} height={140} /> : null}
      {!loading && error && list.length === 0 ? (
        <BrutalEmpty title="Couldn't load gyms" message={error} actionTitle="Retry" onAction={() => dispatch(fetchGyms())} />
      ) : null}
      {!loading && !error && list.length === 0 ? (
        <BrutalEmpty title="No gym profile yet" message="Add your gym's details." actionTitle="Add gym" onAction={() => navigation.navigate('GymForm')} />
      ) : null}

      {list.map((item) => (
        <GymCard key={item.id} gym={item} onEdit={() => navigation.navigate('GymForm', { gym: item })} onDelete={() => setToDelete(item)} />
      ))}

      <ConfirmModal
        visible={!!toDelete}
        title={`Delete "${stringValue(toDelete?.gym_name)}"?`}
        message="This gym profile will be removed from the list."
        onConfirm={onDelete}
        onCancel={() => setToDelete(null)}
      />
    </BrutalScreen>
  );
};
