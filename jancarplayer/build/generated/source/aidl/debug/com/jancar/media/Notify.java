/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: G:\\JanCar\\JancarPlayers\\jancarplayer\\src\\main\\aidl\\com\\jancar\\media\\Notify.aidl
 */
package com.jancar.media;
// Declare any non-default types here with import statements

public interface Notify extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.jancar.media.Notify
{
private static final java.lang.String DESCRIPTOR = "com.jancar.media.Notify";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.jancar.media.Notify interface,
 * generating a proxy if needed.
 */
public static com.jancar.media.Notify asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.jancar.media.Notify))) {
return ((com.jancar.media.Notify)iin);
}
return new com.jancar.media.Notify.Stub.Proxy(obj);
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
case TRANSACTION_notifyMusic:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.jancar.media.data.Music> _arg0;
_arg0 = data.createTypedArrayList(com.jancar.media.data.Music.CREATOR);
this.notifyMusic(_arg0);
reply.writeNoException();
reply.writeTypedList(_arg0);
return true;
}
case TRANSACTION_notifyVideo:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.jancar.media.data.Video> _arg0;
_arg0 = data.createTypedArrayList(com.jancar.media.data.Video.CREATOR);
this.notifyVideo(_arg0);
reply.writeNoException();
reply.writeTypedList(_arg0);
return true;
}
case TRANSACTION_notifyImage:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.jancar.media.data.Image> _arg0;
_arg0 = data.createTypedArrayList(com.jancar.media.data.Image.CREATOR);
this.notifyImage(_arg0);
reply.writeNoException();
reply.writeTypedList(_arg0);
return true;
}
case TRANSACTION_notifyID3Music:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<com.jancar.media.data.Music> _arg0;
_arg0 = data.createTypedArrayList(com.jancar.media.data.Music.CREATOR);
this.notifyID3Music(_arg0);
reply.writeNoException();
reply.writeTypedList(_arg0);
return true;
}
case TRANSACTION_notifyPath:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
this.notifyPath(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.jancar.media.Notify
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
     */
@Override public void notifyMusic(java.util.List<com.jancar.media.data.Music> list) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(list);
mRemote.transact(Stub.TRANSACTION_notifyMusic, _data, _reply, 0);
_reply.readException();
_reply.readTypedList(list, com.jancar.media.data.Music.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void notifyVideo(java.util.List<com.jancar.media.data.Video> list) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(list);
mRemote.transact(Stub.TRANSACTION_notifyVideo, _data, _reply, 0);
_reply.readException();
_reply.readTypedList(list, com.jancar.media.data.Video.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void notifyImage(java.util.List<com.jancar.media.data.Image> list) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(list);
mRemote.transact(Stub.TRANSACTION_notifyImage, _data, _reply, 0);
_reply.readException();
_reply.readTypedList(list, com.jancar.media.data.Image.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void notifyID3Music(java.util.List<com.jancar.media.data.Music> list) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeTypedList(list);
mRemote.transact(Stub.TRANSACTION_notifyID3Music, _data, _reply, 0);
_reply.readException();
_reply.readTypedList(list, com.jancar.media.data.Music.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void notifyPath(java.lang.String path) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(path);
mRemote.transact(Stub.TRANSACTION_notifyPath, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_notifyMusic = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_notifyVideo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_notifyImage = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_notifyID3Music = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_notifyPath = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
}
/**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
public void notifyMusic(java.util.List<com.jancar.media.data.Music> list) throws android.os.RemoteException;
public void notifyVideo(java.util.List<com.jancar.media.data.Video> list) throws android.os.RemoteException;
public void notifyImage(java.util.List<com.jancar.media.data.Image> list) throws android.os.RemoteException;
public void notifyID3Music(java.util.List<com.jancar.media.data.Music> list) throws android.os.RemoteException;
public void notifyPath(java.lang.String path) throws android.os.RemoteException;
}
