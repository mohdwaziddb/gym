import React, { useCallback, useEffect, useState } from 'react';
import { View, FlatList, Text, StyleSheet, ActivityIndicator, RefreshControl } from 'react-native';
import { useFocusEffect } from '@react-navigation/native';
import { BrutalScreen } from '../components/brutal/BrutalScreen';
import { BrutalHeader } from '../components/brutal/BrutalHeader';
import { BrutalButton } from '../components/brutal/BrutalButton';
import { SearchBar } from '../components/brutal/SearchBar';
import { ChipSelect } from '../components/brutal/ChipSelect';
import { Skeleton } from '../components/brutal/Skeleton';
import { BrutalEmpty } from '../components/brutal/BrutalEmpty';
import { BottomBar } from '../components/brutal/BottomBar';
import { MemberRow } from '../components/member/MemberRow';
import { useAppDispatch, useAppSelector } from '../hooks/hooks';
import { fetchMembers, setMemberSearch, setMemberStatus } from '../store/memberSlice';
import { COLORS, SPACING, TYPE } from '../theme/tokens';

const STATUS_OPTIONS = [
  { value: '', label: 'All' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'EXPIRING', label: 'Expiring' },
  { value: 'EXPIRED', label: 'Expired' },
  { value: 'NO_PLAN', label: 'No plan' },
  { value: 'UPCOMING', label: 'Upcoming' },
];

export const MembersScreen = ({ navigation }: any) => {
  const dispatch = useAppDispatch();
  const { list, total, hasMore, loading, loadingMore, error, search, status } = useAppSelector((s) => s.member);
  const [query, setQuery] = useState(search);

  // Screen pe aate hi (Add/Edit/Assign ke baad bhi) aur status chip badalne pe list fresh
  useFocusEffect(
    useCallback(() => {
      setQuery(search);
      dispatch(fetchMembers({ reset: true }));
    }, [dispatch, status]),
  );

  // Search typing ruke tab hi API call (har akshar pe nahi)
  useEffect(() => {
    if (query === search) return;
    const t = setTimeout(() => {
      dispatch(setMemberSearch(query));
      dispatch(fetchMembers({ reset: true }));
    }, 400);
    return () => clearTimeout(t);
  }, [query]);

  const onStatus = (value: string) => {
    dispatch(setMemberStatus(value));
  };

  const loadMore = () => {
    if (hasMore && !loading && !loadingMore) dispatch(fetchMembers({ reset: false }));
  };

  const header = (
    <View>
      <BrutalHeader
        section="02 / MEMBERS"
        title="Members"
        subtitle={loading ? 'Loading…' : `${total} ${total === 1 ? 'member' : 'members'}${status ? ` · ${status.replace('_', ' ')}` : ''}`}
        right={<BrutalButton title="+ Add" small onPress={() => navigation.navigate('MemberForm')} />}
      />
      <SearchBar value={query} onChangeText={setQuery} />
      <ChipSelect horizontal options={STATUS_OPTIONS} value={status} onChange={onStatus} />
      {loading ? <Skeleton rows={5} /> : null}
      {!loading && error ? (
        <BrutalEmpty title="Couldn't load members" message={error} actionTitle="Retry" onAction={() => dispatch(fetchMembers({ reset: true }))} />
      ) : null}
    </View>
  );

  return (
    <BrutalScreen scroll={false} footer={<BottomBar active="Members" navigation={navigation} />}>
      <FlatList
        data={loading ? [] : list}
        keyExtractor={(item) => String(item.id)}
        ListHeaderComponent={header}
        renderItem={({ item }) => <MemberRow member={item} onPress={() => navigation.navigate('MemberDetail', { id: item.id })} />}
        onEndReached={loadMore}
        onEndReachedThreshold={0.4}
        keyboardShouldPersistTaps="handled"
        refreshControl={<RefreshControl refreshing={false} onRefresh={() => dispatch(fetchMembers({ reset: true }))} tintColor={COLORS.ink} />}
        ListEmptyComponent={
          !loading && !error ? (
            <BrutalEmpty
              title={search || status ? 'No matching members' : 'No members yet'}
              message={search || status ? 'Try a different search or filter.' : 'Register your first member to get started.'}
              actionTitle={search || status ? undefined : 'Add member'}
              onAction={() => navigation.navigate('MemberForm')}
            />
          ) : null
        }
        ListFooterComponent={
          loadingMore ? <ActivityIndicator color={COLORS.ink} style={styles.footer} /> : list.length > 0 && !hasMore ? <Text style={styles.end}>— END OF LIST —</Text> : null
        }
        contentContainerStyle={styles.listContent}
      />
    </BrutalScreen>
  );
};

const styles = StyleSheet.create({
  listContent: { paddingBottom: SPACING.xxl },
  footer: { marginVertical: SPACING.lg },
  end: { ...TYPE.label, textAlign: 'center', color: COLORS.muted, marginVertical: SPACING.lg },
});
