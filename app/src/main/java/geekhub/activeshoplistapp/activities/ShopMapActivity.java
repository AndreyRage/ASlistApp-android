package geekhub.activeshoplistapp.activities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import geekhub.activeshoplistapp.R;
import geekhub.activeshoplistapp.helpers.AppConstants;
import geekhub.activeshoplistapp.helpers.ShoppingHelper;
import geekhub.activeshoplistapp.model.ShopsModel;

/**
 * Created by rage on 3/3/15.
 */
public class ShopMapActivity extends BaseActivity implements OnMapReadyCallback {
    private ShopsModel shop;
    private GoogleMap map;
    private EditText shopNameEdit;
    private boolean isEdit = false;
    private boolean isOnceShowMyLocation = false;
    private Marker marker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        map = mapFragment.getMap();
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);

        shopNameEdit = (EditText) findViewById(R.id.title);

        Intent args = getIntent();
        int id = -1;
        if (args != null) {
            id = args.getIntExtra(AppConstants.EXTRA_SHOP_ID, 0);
        }
        if (id >= 0) {
            shop = ShoppingHelper.getInstance().getShopsList().get(id);
            isEdit = true;
            marker = map.addMarker(new MarkerOptions()
                    .position(new LatLng(
                            shop.getGpsLatitude(),
                            shop.getGpsLongitude()
                    ))
                    .draggable(true));
            shopNameEdit.setText(shop.getShopName());
        } else {
            shop = new ShopsModel();
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (!isOnceShowMyLocation) {
                    if (!isEdit) {
                        showMyLocation(location);
                        isOnceShowMyLocation = true;
                    } else {
                        LatLng latLng = new LatLng(shop.getGpsLatitude(), shop.getGpsLongitude());
                        showMyLocation(latLng);
                    }
                }
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (marker == null) {
                    marker = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .draggable(true));
                } else {
                    marker.setPosition(latLng);
                }
            }
        });
    }

    private void showMyLocation(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        showMyLocation(latLng);
    }

    private void showMyLocation(LatLng latLng) {
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(latLng, 17)
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (!isEdit && marker != null) {
            shop.setGpsLatitude(marker.getPosition().latitude);
            shop.setGpsLongitude(marker.getPosition().longitude);
            shop.setShopName(shopNameEdit.getText().toString());
            ShoppingHelper.getInstance().addShop(shop);
        }
        if (isEdit) {
            boolean edit = false;
            if (shop.getGpsLatitude() != marker.getPosition().latitude
                    && shop.getGpsLongitude() != marker.getPosition().longitude) {
                edit = true;
                shop.setGpsLatitude(marker.getPosition().latitude);
                shop.setGpsLongitude(marker.getPosition().longitude);
            }
            if (!TextUtils.equals(shopNameEdit.getText().toString(), shop.getShopName())) {
                edit = true;
                shop.setShopName(shopNameEdit.getText().toString());
            }
            if (edit) {
                ShoppingHelper.getInstance().updateShop(shop);
            }
        }
    }
}