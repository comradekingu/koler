package com.chooloo.www.koler.ui.contacts

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.chooloo.www.koler.R
import com.chooloo.www.koler.adapter.ContactsAdapter
import com.chooloo.www.koler.data.Contact
import com.chooloo.www.koler.data.ContactsBundle
import com.chooloo.www.koler.livedata.ContactsProviderLiveData
import com.chooloo.www.koler.ui.base.BottomFragment
import com.chooloo.www.koler.ui.contact.ContactFragment
import com.chooloo.www.koler.ui.list.ListFragment
import com.chooloo.www.koler.viewmodel.SearchViewModel

class ContactsFragment : ListFragment<Contact, ContactsAdapter>(), ContactsContract.View {
    private var _onContactsChangedListener: (ContactsBundle) -> Unit? = {}
    private val _contactsLiveData by lazy { ContactsProviderLiveData(_activity) }
    private val _presenter by lazy { ContactsPresenter<ContactsContract.View>() }
    private val _searchViewModel by lazy { ViewModelProvider(requireActivity()).get(SearchViewModel::class.java) }

    override val requiredPermissions by lazy { _contactsLiveData.requiredPermissions }
    override val noResultsMessage by lazy { getString(R.string.error_no_results_contacts) }
    override val noPermissionsMessage by lazy { getString(R.string.error_no_permissions_contacts) }
    override val adapter by lazy {
        ContactsAdapter().apply {
            setOnItemClickListener(_presenter::onContactItemClick)
            setOnItemLongClickListener(_presenter::onContactItemLongClick)
        }
    }
    //endregion

    companion object {
        const val ARG_OBSERVE_SEARCH = "observe_search"

        fun newInstance(isCompact: Boolean = false, observeSearch: Boolean = false) =
            ContactsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_COMPACT, isCompact)
                    putBoolean(ARG_OBSERVE_SEARCH, observeSearch)
                }
            }
    }

    override fun onSetup() {
        super.onSetup()
        _presenter.attach(this)
    }

    override fun onAttachData() {
        _contactsLiveData.observe(viewLifecycleOwner) {
            _presenter.onContactsChanged(it)
            _onContactsChangedListener.invoke(it)
        }
        if (argsSafely.getBoolean(ARG_OBSERVE_SEARCH)) {
            _searchViewModel.contactsText.observe(viewLifecycleOwner, ::setContactsFilter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _presenter.detach()
    }

    override fun openContact(contact: Contact) {
        BottomFragment(ContactFragment.newInstance(contact.id)).show(
            _activity.supportFragmentManager,
            ContactFragment.TAG
        )
    }

    override fun updateContacts(contactsBundle: ContactsBundle) {
        adapter.data = contactsBundle.listBundleByLettersWithFavs
    }

    override fun setContactsFilter(filter: String?) {
        _contactsLiveData.filter = filter
    }

    fun setOnContactsChangedListener(onContactsChangedListener: (ContactsBundle) -> Unit? = {}) {
        _onContactsChangedListener = onContactsChangedListener
    }
}