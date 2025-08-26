package com.codingempire.adminpse.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.codingempire.adminpse.fragments.withdraw.AllWithdrawalsRequestFragment
import com.trustledger.adminaitrust.fragments.withdraw.ApprovedWithdrawalsFragment
import com.codingempire.adminpse.fragments.withdraw.RejectedWithdrawalsFragment

class WithdrawalsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllWithdrawalsRequestFragment()
            1 -> ApprovedWithdrawalsFragment()
            2 -> RejectedWithdrawalsFragment()
            else -> AllWithdrawalsRequestFragment()
        }
    }
}
