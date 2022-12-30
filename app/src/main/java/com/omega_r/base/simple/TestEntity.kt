package com.omega_r.base.simple

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import java.io.Serializable

class TestEntity() : Parcelable {

    constructor(parcel: Parcel) : this() {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Creator<TestEntity> {

        override fun createFromParcel(parcel: Parcel): TestEntity {
            return TestEntity(parcel)
        }

        override fun newArray(size: Int): Array<TestEntity?> {
            return arrayOfNulls(size)
        }
    }
}