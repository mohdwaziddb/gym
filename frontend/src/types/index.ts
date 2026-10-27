export type Gym = {
  id: number | null;
  gym_name: string;
  gym_code: string;
  owner_name?: string;
  contact_no?: string;
  email?: string;
  address?: string;
  city?: string;
  monthly_fees?: string;
  is_active?: boolean;
  tenant?: string;
};

export type User = {
  user_id: number;
  username: string;
  gym_code: string;
  db_name: string;
  role: string;
  token: string;
};

// ---------- Phase 1: Members / Plans / Memberships / Dashboard ----------

export type MemberStatus = 'ACTIVE' | 'EXPIRING' | 'EXPIRED' | 'UPCOMING' | 'NO_PLAN';

export type MemberListItem = {
  id: number;
  member_code: string;
  full_name: string;
  phone: string;
  email?: string;
  gender?: string;
  joining_date: string;
  expiry_date: string;
  status: MemberStatus;
};

export type Membership = {
  id: number;
  plan_id: number;
  plan_name: string;
  start_date: string;
  end_date: string;
  price: string;
  discount: string;
  final_amount: string;
  status: 'ACTIVE' | 'CANCELLED';
  payment_status: string;
  renewed_from_id: number | null;
  days_left: number;
  created_on: string;
};

export type MemberDetail = {
  id: number;
  member_code: string;
  full_name: string;
  phone: string;
  email: string;
  gender: string;
  dob: string;
  address: string;
  emergency_name: string;
  emergency_phone: string;
  joining_date: string;
  notes?: string; // sirf owner/admin ko aata hai
  status: MemberStatus;
  current_membership: Membership | null;
  upcoming_membership: Membership | null;
  expiry_date: string;
  renewal_start_date: string;
  latest_membership_id: number | null;
  memberships: Membership[];
};

export type DurationType = 'MONTHLY' | 'QUARTERLY' | 'HALF_YEARLY' | 'YEARLY' | 'CUSTOM';

export type MembershipPlan = {
  id: number;
  plan_name: string;
  duration_type: DurationType;
  duration_days: number;
  price: string;
  description: string;
  is_active: boolean;
};

export type DashboardSummary = {
  total_members: number;
  active: number;
  expiring: number;
  expired: number;
  upcoming: number;
  no_plan: number;
  new_members_this_month: number;
  memberships_assigned_this_month: number;
  membership_value_assigned: string;
  expiring_within_days: number;
  as_of: string;
};
