package com.jsw.tweetmap.ui.main

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.airbnb.android.airmapview.AirMapMarker
import com.airbnb.android.airmapview.AirMapView
import com.airbnb.android.airmapview.listeners.OnMapInitializedListener
import com.airbnb.android.airmapview.listeners.OnMapMarkerClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout
import com.jsw.tweetmap.MainActivity
import com.jsw.tweetmap.R
import com.jsw.tweetmap.interfaces.BundleKeys
import com.jsw.tweetmap.model.Tweet
import com.ohoussein.playpause.PlayPauseView


class MainFragment : Fragment() {
    private lateinit var mapView: AirMapView
    private var savedState: Bundle? = null
    private lateinit var searchButton: PlayPauseView
    private lateinit var searchText: TextInputLayout
    private var syncDelay = 5
    private lateinit var viewModel: MainViewModel

    /*** OVERRIDE FUNCIONS ***/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)
        mapView = view.findViewById(R.id.map_view)
        searchButton = view.findViewById(R.id.btn_search)
        searchText = view.findViewById(R.id.til_search)

        val uiHandler = Handler()
        uiHandler.post { mapView.initialize(getFragmentManager()) }

        mapView.setOnMapInitializedListener(object : OnMapInitializedListener {
            override fun onMapInitialized() {
                if (savedState != null) {
                    mapView.setCenterZoom(savedState?.getParcelable(BundleKeys.MAP_CENTER),
                        savedState?.getInt(BundleKeys.MAP_ZOOM)!!)
                    createObserver()
                }
            }
        })

        searchButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (searchButton.isPlay == true) {
                    searchButton.change(!searchButton.isPlay, true)
                    activity?.hideKeyboard(view)
                    createObserver()
                } else {
                    viewModel.stopSearch()
                    searchButton.change(!searchButton.isPlay)
                }
            }
        })

        mapView.setOnMarkerClickListener(object : OnMapMarkerClickListener {
            override fun onMapMarkerClick(airMarker: AirMapMarker<*>?) {
                val tweet = viewModel.getTweetByID(airMarker?.id)
                (activity as MainActivity).openDetails(tweet)
            }
        })

        if (savedState != null) {
            syncDelay = savedState?.getInt(BundleKeys.SYNC_DELAY)!!
            searchText.editText?.setText(savedState?.getString(BundleKeys.SEARCH_TEXT))
            searchButton.change(savedState?.getBoolean(BundleKeys.BUTTON_STATE)!!)
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)

        // Tint drawable
        var drawable = menu.findItem(R.id.menu_time).icon
        drawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(drawable, ContextCompat.getColor(requireContext(), R.color.colorAccent))

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.getItemId()

        //Create & open the number picker dialog
        if (id == R.id.menu_time) {
            val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
            val inflater = this.layoutInflater
            val dialogView: View = inflater.inflate(R.layout.numpick_dialog, null)
            val numberPicker = dialogView.findViewById(R.id.dialog_number_picker) as NumberPicker

            dialogBuilder.setTitle(getString(R.string.numpick_title))
            dialogBuilder.setMessage(getString(R.string.numpick_msg))
            dialogBuilder.setView(dialogView)
            dialogBuilder.setPositiveButton("Done") { _, _ -> syncDelay = numberPicker.value }

            numberPicker.maxValue = 59
            numberPicker.minValue = 1
            numberPicker.value = syncDelay
            numberPicker.wrapSelectorWheel = false

            val alertDialog: AlertDialog = dialogBuilder.create()
            alertDialog.show()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        // Save our instance vars into bundle
        savedState = Bundle()
        savedState?.putInt(BundleKeys.SYNC_DELAY, syncDelay)
        savedState?.putInt(BundleKeys.MAP_ZOOM, mapView.zoom)
        savedState?.putBoolean(BundleKeys.BUTTON_STATE, searchButton.isPlay)
        savedState?.putString(BundleKeys.SEARCH_TEXT, searchText.editText?.text.toString())
        savedState?.putParcelable(BundleKeys.MAP_CENTER, mapView.center)
        viewModel.stopSearch()
        super.onDestroyView()
    }


    /*** SUPPORT FUNCTIONS ***/
    private fun createObserver() {
        viewModel.getTweets(searchText.editText?.text.toString(), syncDelay)
            .observe(viewLifecycleOwner,
                object : Observer<List<Tweet>?> {
                    override fun onChanged(t: List<Tweet>?) {
                        if (t != null) {
                            for (tmp: Tweet in t) {
                                mapView.addMarker(
                                    AirMapMarker.Builder<Any?>()
                                        .position(LatLng(tmp.geoLocation.latitude, tmp.geoLocation.longitude))
                                        .iconId(R.drawable.twitter)
                                        .title(tmp.user)
                                        .id(tmp.id)
                                        .build()
                                )
                            }
                        } else {
                            mapView.clearMarkers()
                        }
                    }
                }
            )
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
