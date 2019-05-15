/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: G:\\JanCar\\JancarPlayers\\usbmediascanner\\src\\main\\aidl\\com\\jancar\\media\\FlyMedia.aidl
 */
package com.jancar.media;
// Declare any non-default types here with import statements

public interface FlyMedia extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.jancar.media.FlyMedia
{
private static final java.lang.String DESCRIPTOR = "com.jancar.media.FlyMedia";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.jancar.media.FlyMedia interface,
 * generating a proxy if needed.
 */
public static com.jancar.media.FlyMedia asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.jancar.media.FlyMedia))) {
return ((com.jancar.media.FlyMedia)iin);
}
return new com.jancar.media.FlyMedia.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_scanDisk:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.scanDisk(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_notify:
{
data.enforceInterface(DESCRIPTOR);
com.jancar.media.Notify _arg0;
_arg0 = com.jancar.media.Notify.Stub.asInterface(data.readStrongBinder());
this.notify(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerNotify:
{
data.enforceInterface(DESCRIPTOR);
com.jancar.media.Notify _arg0;
_arg0 = com.jancar.media.Notify.Stub.asInterface(data.readStrongBinder());
this.registerNotify(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterNotify:
{
data.enforceInterface(DESCRIPTOR);
com.jancar.media.Notify _arg0;
_arg0 = com.jancar.media.Notify.Stub.asInterface(data.readStrongBinder());
this.unregisterNotify(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.jancar.media.FlyMedia
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     *
     */
@Override public void scanDisk(java.lang.String disk) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(disk);
mRemote.transact(Stub.TRANSACTION_scanDisk, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void notify(com.jancar.media.Notify notify) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((notify!=null))?(notify.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_notify, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void registerNotify(com.jancar.media.Notify notify) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((notify!=null))?(notify.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerNotify, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void unregisterNotify(com.jancar.media.Notify notify) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((notify!=null))?(notify.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterNotify, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_scanDisk = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_notify = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_registerNotify = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_unregisterNotify = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     *
     */
public void scanDisk(java.lang.String disk) throws android.os.RemoteException;
public void notify(com.jancar.media.Notify notify) throws android.os.RemoteException;
public void registerNotify(com.jancar.media.Notify notify) throws android.os.RemoteException;
public void unregisterNotify(com.jancar.media.Notify notify) throws android.os.RemoteException;
}
