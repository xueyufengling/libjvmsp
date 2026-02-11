package jvmsp.libso;

import java.lang.invoke.MethodHandle;

import jvmsp.memory;
import jvmsp.type.cxx_type;
import jvmsp.type.cxx_type.function_pointer_type;
import jvmsp.type.cxx_type.pointer;
import jvmsp.type.java_type;

/**
 * JNINativeInterface_的函数封装
 */
public class jni_native_interface
{
	public static final cxx_type JNINativeInterface_ = cxx_type.define("JNINativeInterface_")
			.decl_field("reserved0", cxx_type.pvoid)
			.decl_field("reserved1", cxx_type.pvoid)
			.decl_field("reserved2", cxx_type.pvoid)
			.decl_field("reserved3", cxx_type.pvoid)
			.decl_field("GetVersion", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv))
			.decl_field("DefineClass", function_pointer_type.of(cxx_type.jclass, libjvm.pJNIEnv, cxx_type.pchar, cxx_type.jobject, cxx_type.pjbyte, cxx_type.jsize))
			.decl_field("FindClass", function_pointer_type.of(cxx_type.jclass, libjvm.pJNIEnv, cxx_type.pchar))
			.decl_field("FromReflectedMethod", function_pointer_type.of(cxx_type.jmethodID, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("FromReflectedField", function_pointer_type.of(cxx_type.jfieldID, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("ToReflectedMethod", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jboolean))
			.decl_field("GetSuperclass", function_pointer_type.of(cxx_type.jclass, libjvm.pJNIEnv, cxx_type.jclass))
			.decl_field("IsAssignableFrom", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jclass))
			.decl_field("ToReflectedField", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jboolean))
			.decl_field("Throw", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jthrowable))
			.decl_field("ThrowNew", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.pchar))
			.decl_field("ExceptionOccurred", function_pointer_type.of(cxx_type.jthrowable, libjvm.pJNIEnv))
			.decl_field("ExceptionDescribe", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv))
			.decl_field("ExceptionClear", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv))
			.decl_field("FatalError", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.pchar))
			.decl_field("PushLocalFrame", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jint))
			.decl_field("PopLocalFrame", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("NewGlobalRef", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("DeleteGlobalRef", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("DeleteLocalRef", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("IsSameObject", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jobject))
			.decl_field("NewLocalRef", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("EnsureLocalCapacity", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jint))
			.decl_field("AllocObject", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass))
			.decl_field("NewObject", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("NewObjectV", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("NewObjectA", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("GetObjectClass", function_pointer_type.of(cxx_type.jclass, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("IsInstanceOf", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass))
			.decl_field("GetMethodID", function_pointer_type.of(cxx_type.jmethodID, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.pchar, cxx_type.pchar))
			.decl_field("CallObjectMethod", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallObjectMethodV", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallObjectMethodA", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallBooleanMethod", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallBooleanMethodV", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallBooleanMethodA", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallByteMethod", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallByteMethodV", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallByteMethodA", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallCharMethod", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallCharMethodV", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallCharMethodA", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallShortMethod", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallShortMethodV", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallShortMethodA", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallIntMethod", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallIntMethodV", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallIntMethodA", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallLongMethod", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallLongMethodV", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallLongMethodA", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallFloatMethod", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallFloatMethodV", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallFloatMethodA", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallDoubleMethod", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallDoubleMethodV", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallDoubleMethodA", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallVoidMethod", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID))
			.decl_field("CallVoidMethodV", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallVoidMethodA", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualObjectMethod", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualObjectMethodV", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualObjectMethodA", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualBooleanMethod", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualBooleanMethodV", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualBooleanMethodA", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualByteMethod", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualByteMethodV", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualByteMethodA", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualCharMethod", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualCharMethodV", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualCharMethodA", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualShortMethod", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualShortMethodV", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualShortMethodA", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualIntMethod", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualIntMethodV", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualIntMethodA", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualLongMethod", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualLongMethodV", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualLongMethodA", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualFloatMethod", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualFloatMethodV", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualFloatMethodA", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualDoubleMethod", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualDoubleMethodV", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualDoubleMethodA", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallNonvirtualVoidMethod", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallNonvirtualVoidMethodV", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallNonvirtualVoidMethodA", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("GetFieldID", function_pointer_type.of(cxx_type.jfieldID, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.pchar, cxx_type.pchar))
			.decl_field("GetObjectField", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetBooleanField", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetByteField", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetCharField", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetShortField", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetIntField", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetLongField", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetFloatField", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("GetDoubleField", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID))
			.decl_field("SetObjectField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jobject))
			.decl_field("SetBooleanField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jboolean))
			.decl_field("SetByteField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jbyte))
			.decl_field("SetCharField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jchar))
			.decl_field("SetShortField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jshort))
			.decl_field("SetIntField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jint))
			.decl_field("SetLongField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jlong))
			.decl_field("SetFloatField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jfloat))
			.decl_field("SetDoubleField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobject, cxx_type.jfieldID, cxx_type.jdouble))
			.decl_field("GetStaticMethodID", function_pointer_type.of(cxx_type.jmethodID, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.pchar, cxx_type.pchar))
			.decl_field("CallStaticObjectMethod", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticObjectMethodV", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticObjectMethodA", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticBooleanMethod", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticBooleanMethodV", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticBooleanMethodA", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticByteMethod", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticByteMethodV", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticByteMethodA", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticCharMethod", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticCharMethodV", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticCharMethodA", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticShortMethod", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticShortMethodV", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticShortMethodA", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticIntMethod", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticIntMethodV", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticIntMethodA", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticLongMethod", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticLongMethodV", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticLongMethodA", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticFloatMethod", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticFloatMethodV", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticFloatMethodA", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticDoubleMethod", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticDoubleMethodV", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticDoubleMethodA", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("CallStaticVoidMethod", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID))
			.decl_field("CallStaticVoidMethodV", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.va_list))
			.decl_field("CallStaticVoidMethodA", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jmethodID, cxx_type.jvalue))
			.decl_field("GetStaticFieldID", function_pointer_type.of(cxx_type.jfieldID, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.pchar, cxx_type.pchar))
			.decl_field("GetStaticObjectField", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticBooleanField", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticByteField", function_pointer_type.of(cxx_type.jbyte, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticCharField", function_pointer_type.of(cxx_type.jchar, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticShortField", function_pointer_type.of(cxx_type.jshort, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticIntField", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticLongField", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticFloatField", function_pointer_type.of(cxx_type.jfloat, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("GetStaticDoubleField", function_pointer_type.of(cxx_type.jdouble, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID))
			.decl_field("SetStaticObjectField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jobject))
			.decl_field("SetStaticBooleanField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jboolean))
			.decl_field("SetStaticByteField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jbyte))
			.decl_field("SetStaticCharField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jchar))
			.decl_field("SetStaticShortField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jshort))
			.decl_field("SetStaticIntField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jint))
			.decl_field("SetStaticLongField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jlong))
			.decl_field("SetStaticFloatField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jfloat))
			.decl_field("SetStaticDoubleField", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jclass, cxx_type.jfieldID, cxx_type.jdouble))
			.decl_field("NewString", function_pointer_type.of(cxx_type.jstring, libjvm.pJNIEnv, cxx_type.pjchar, cxx_type.jsize))
			.decl_field("GetStringLength", function_pointer_type.of(cxx_type.jsize, libjvm.pJNIEnv, cxx_type.jstring))
			.decl_field("GetStringChars", function_pointer_type.of(cxx_type.pjchar, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.pjboolean))
			.decl_field("ReleaseStringChars", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.pjchar))
			.decl_field("NewStringUTF", function_pointer_type.of(cxx_type.jstring, libjvm.pJNIEnv, cxx_type.pchar))
			.decl_field("GetStringUTFLength", function_pointer_type.of(cxx_type.jsize, libjvm.pJNIEnv, cxx_type.jstring))
			.decl_field("GetStringUTFChars", function_pointer_type.of(cxx_type.pchar, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.pjboolean))
			.decl_field("ReleaseStringUTFChars", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.pchar))
			.decl_field("GetArrayLength", function_pointer_type.of(cxx_type.jsize, libjvm.pJNIEnv, cxx_type.jarray))
			.decl_field("NewObjectArray", function_pointer_type.of(cxx_type.jobjectArray, libjvm.pJNIEnv, cxx_type.jsize, cxx_type.jclass, cxx_type.jobject))
			.decl_field("GetObjectArrayElement", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jobjectArray, cxx_type.jsize))
			.decl_field("SetObjectArrayElement", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jobjectArray, cxx_type.jsize, cxx_type.jobject))
			.decl_field("NewBooleanArray", function_pointer_type.of(cxx_type.jbooleanArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewByteArray", function_pointer_type.of(cxx_type.jbyteArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewCharArray", function_pointer_type.of(cxx_type.jcharArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewShortArray", function_pointer_type.of(cxx_type.jshortArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewIntArray", function_pointer_type.of(cxx_type.jintArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewLongArray", function_pointer_type.of(cxx_type.jlongArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewFloatArray", function_pointer_type.of(cxx_type.jfloatArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("NewDoubleArray", function_pointer_type.of(cxx_type.jdoubleArray, libjvm.pJNIEnv, cxx_type.jsize))
			.decl_field("GetBooleanArrayElements", function_pointer_type.of(cxx_type.pjboolean, libjvm.pJNIEnv, cxx_type.jbooleanArray, cxx_type.pjboolean))
			.decl_field("GetByteArrayElements", function_pointer_type.of(cxx_type.pjbyte, libjvm.pJNIEnv, cxx_type.jbyteArray, cxx_type.pjboolean))
			.decl_field("GetCharArrayElements", function_pointer_type.of(cxx_type.pjchar, libjvm.pJNIEnv, cxx_type.jcharArray, cxx_type.pjboolean))
			.decl_field("GetShortArrayElements", function_pointer_type.of(cxx_type.pjshort, libjvm.pJNIEnv, cxx_type.jshortArray, cxx_type.pjboolean))
			.decl_field("GetIntArrayElements", function_pointer_type.of(cxx_type.pjint, libjvm.pJNIEnv, cxx_type.jintArray, cxx_type.pjboolean))
			.decl_field("GetLongArrayElements", function_pointer_type.of(cxx_type.pjlong, libjvm.pJNIEnv, cxx_type.jlongArray, cxx_type.pjboolean))
			.decl_field("GetFloatArrayElements", function_pointer_type.of(cxx_type.pjfloat, libjvm.pJNIEnv, cxx_type.jfloatArray, cxx_type.pjboolean))
			.decl_field("GetDoubleArrayElements", function_pointer_type.of(cxx_type.pjdouble, libjvm.pJNIEnv, cxx_type.jdoubleArray, cxx_type.pjboolean))
			.decl_field("ReleaseBooleanArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jbooleanArray, cxx_type.pjboolean, cxx_type.jint))
			.decl_field("ReleaseByteArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jbyteArray, cxx_type.pjbyte, cxx_type.jint))
			.decl_field("ReleaseCharArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jcharArray, cxx_type.pjchar, cxx_type.jint))
			.decl_field("ReleaseShortArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jshortArray, cxx_type.pjshort, cxx_type.jint))
			.decl_field("ReleaseIntArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jintArray, cxx_type.pjint, cxx_type.jint))
			.decl_field("ReleaseLongArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jlongArray, cxx_type.pjlong, cxx_type.jint))
			.decl_field("ReleaseFloatArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jfloatArray, cxx_type.pjfloat, cxx_type.jint))
			.decl_field("ReleaseDoubleArrayElements", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jdoubleArray, cxx_type.pjdouble, cxx_type.jint))
			.decl_field("GetBooleanArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jbooleanArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjboolean))
			.decl_field("GetByteArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jbyteArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjbyte))
			.decl_field("GetCharArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jcharArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjchar))
			.decl_field("GetShortArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jshortArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjshort))
			.decl_field("GetIntArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jintArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjint))
			.decl_field("GetLongArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jlongArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjlong))
			.decl_field("GetFloatArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jfloatArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjfloat))
			.decl_field("GetDoubleArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jdoubleArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjdouble))
			.decl_field("SetBooleanArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jbooleanArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjboolean))
			.decl_field("SetByteArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jbyteArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjbyte))
			.decl_field("SetCharArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jcharArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjchar))
			.decl_field("SetShortArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jshortArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjshort))
			.decl_field("SetIntArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jintArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjint))
			.decl_field("SetLongArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jlongArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjlong))
			.decl_field("SetFloatArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jfloatArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjfloat))
			.decl_field("SetDoubleArrayRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jdoubleArray, cxx_type.jsize, cxx_type.jsize, cxx_type.pjdouble))
			.decl_field("RegisterNatives", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass, libjvm.pJNINativeMethod, cxx_type.jint))
			.decl_field("UnregisterNatives", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jclass))
			.decl_field("MonitorEnter", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("MonitorExit", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("GetJavaVM", function_pointer_type.of(cxx_type.jint, libjvm.pJNIEnv, libjvm.JavaVM))
			.decl_field("GetStringRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.jsize, cxx_type.jsize, cxx_type.pjchar))
			.decl_field("GetStringUTFRegion", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.jsize, cxx_type.jsize, cxx_type.pchar))
			.decl_field("GetPrimitiveArrayCritical", function_pointer_type.of(cxx_type.pvoid, libjvm.pJNIEnv, cxx_type.jarray, cxx_type.pjboolean))
			.decl_field("ReleasePrimitiveArrayCritical", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jarray, cxx_type.pvoid, cxx_type.jint))
			.decl_field("GetStringCritical", function_pointer_type.of(cxx_type.pjchar, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.pjboolean))
			.decl_field("ReleaseStringCritical", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jstring, cxx_type.pjchar))
			.decl_field("NewWeakGlobalRef", function_pointer_type.of(cxx_type.jweak, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("DeleteWeakGlobalRef", function_pointer_type.of(cxx_type._void, libjvm.pJNIEnv, cxx_type.jweak))
			.decl_field("ExceptionCheck", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv))
			.decl_field("NewDirectByteBuffer", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.pvoid, cxx_type.jlong))
			.decl_field("GetDirectBufferAddress", function_pointer_type.of(cxx_type.pvoid, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("GetDirectBufferCapacity", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("GetObjectRefType", function_pointer_type.of(cxx_type.jobjectRefType, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("GetModule", function_pointer_type.of(cxx_type.jobject, libjvm.pJNIEnv, cxx_type.jclass))
			.decl_field("IsVirtualThread", function_pointer_type.of(cxx_type.jboolean, libjvm.pJNIEnv, cxx_type.jobject))
			.decl_field("GetStringUTFLengthAsLong", function_pointer_type.of(cxx_type.jlong, libjvm.pJNIEnv, cxx_type.jstring))
			.resolve();

	private final cxx_type.object JNINativeInterface_base;

	private final MethodHandle GetVersion;
	private final MethodHandle DefineClass;
	private final MethodHandle FindClass;
	private final MethodHandle FromReflectedMethod;
	private final MethodHandle FromReflectedField;
	private final MethodHandle ToReflectedMethod;
	private final MethodHandle GetSuperclass;
	private final MethodHandle IsAssignableFrom;
	private final MethodHandle ToReflectedField;
	private final MethodHandle Throw;
	private final MethodHandle ThrowNew;
	private final MethodHandle ExceptionOccurred;
	private final MethodHandle ExceptionDescribe;
	private final MethodHandle ExceptionClear;
	private final MethodHandle FatalError;
	private final MethodHandle PushLocalFrame;
	private final MethodHandle PopLocalFrame;
	private final MethodHandle NewGlobalRef;
	private final MethodHandle DeleteGlobalRef;
	private final MethodHandle DeleteLocalRef;
	private final MethodHandle IsSameObject;
	private final MethodHandle NewLocalRef;
	private final MethodHandle EnsureLocalCapacity;
	private final MethodHandle AllocObject;
	private final MethodHandle NewObject;
	private final MethodHandle NewObjectV;
	private final MethodHandle NewObjectA;
	private final MethodHandle GetObjectClass;
	private final MethodHandle IsInstanceOf;
	private final MethodHandle GetMethodID;
	private final MethodHandle CallObjectMethod;
	private final MethodHandle CallObjectMethodV;
	private final MethodHandle CallObjectMethodA;
	private final MethodHandle CallBooleanMethod;
	private final MethodHandle CallBooleanMethodV;
	private final MethodHandle CallBooleanMethodA;
	private final MethodHandle CallByteMethod;
	private final MethodHandle CallByteMethodV;
	private final MethodHandle CallByteMethodA;
	private final MethodHandle CallCharMethod;
	private final MethodHandle CallCharMethodV;
	private final MethodHandle CallCharMethodA;
	private final MethodHandle CallShortMethod;
	private final MethodHandle CallShortMethodV;
	private final MethodHandle CallShortMethodA;
	private final MethodHandle CallIntMethod;
	private final MethodHandle CallIntMethodV;
	private final MethodHandle CallIntMethodA;
	private final MethodHandle CallLongMethod;
	private final MethodHandle CallLongMethodV;
	private final MethodHandle CallLongMethodA;
	private final MethodHandle CallFloatMethod;
	private final MethodHandle CallFloatMethodV;
	private final MethodHandle CallFloatMethodA;
	private final MethodHandle CallDoubleMethod;
	private final MethodHandle CallDoubleMethodV;
	private final MethodHandle CallDoubleMethodA;
	private final MethodHandle CallVoidMethod;
	private final MethodHandle CallVoidMethodV;
	private final MethodHandle CallVoidMethodA;
	private final MethodHandle CallNonvirtualObjectMethod;
	private final MethodHandle CallNonvirtualObjectMethodV;
	private final MethodHandle CallNonvirtualObjectMethodA;
	private final MethodHandle CallNonvirtualBooleanMethod;
	private final MethodHandle CallNonvirtualBooleanMethodV;
	private final MethodHandle CallNonvirtualBooleanMethodA;
	private final MethodHandle CallNonvirtualByteMethod;
	private final MethodHandle CallNonvirtualByteMethodV;
	private final MethodHandle CallNonvirtualByteMethodA;
	private final MethodHandle CallNonvirtualCharMethod;
	private final MethodHandle CallNonvirtualCharMethodV;
	private final MethodHandle CallNonvirtualCharMethodA;
	private final MethodHandle CallNonvirtualShortMethod;
	private final MethodHandle CallNonvirtualShortMethodV;
	private final MethodHandle CallNonvirtualShortMethodA;
	private final MethodHandle CallNonvirtualIntMethod;
	private final MethodHandle CallNonvirtualIntMethodV;
	private final MethodHandle CallNonvirtualIntMethodA;
	private final MethodHandle CallNonvirtualLongMethod;
	private final MethodHandle CallNonvirtualLongMethodV;
	private final MethodHandle CallNonvirtualLongMethodA;
	private final MethodHandle CallNonvirtualFloatMethod;
	private final MethodHandle CallNonvirtualFloatMethodV;
	private final MethodHandle CallNonvirtualFloatMethodA;
	private final MethodHandle CallNonvirtualDoubleMethod;
	private final MethodHandle CallNonvirtualDoubleMethodV;
	private final MethodHandle CallNonvirtualDoubleMethodA;
	private final MethodHandle CallNonvirtualVoidMethod;
	private final MethodHandle CallNonvirtualVoidMethodV;
	private final MethodHandle CallNonvirtualVoidMethodA;
	private final MethodHandle GetFieldID;
	private final MethodHandle GetObjectField;
	private final MethodHandle GetBooleanField;
	private final MethodHandle GetByteField;
	private final MethodHandle GetCharField;
	private final MethodHandle GetShortField;
	private final MethodHandle GetIntField;
	private final MethodHandle GetLongField;
	private final MethodHandle GetFloatField;
	private final MethodHandle GetDoubleField;
	private final MethodHandle SetObjectField;
	private final MethodHandle SetBooleanField;
	private final MethodHandle SetByteField;
	private final MethodHandle SetCharField;
	private final MethodHandle SetShortField;
	private final MethodHandle SetIntField;
	private final MethodHandle SetLongField;
	private final MethodHandle SetFloatField;
	private final MethodHandle SetDoubleField;
	private final MethodHandle GetStaticMethodID;
	private final MethodHandle CallStaticObjectMethod;
	private final MethodHandle CallStaticObjectMethodV;
	private final MethodHandle CallStaticObjectMethodA;
	private final MethodHandle CallStaticBooleanMethod;
	private final MethodHandle CallStaticBooleanMethodV;
	private final MethodHandle CallStaticBooleanMethodA;
	private final MethodHandle CallStaticByteMethod;
	private final MethodHandle CallStaticByteMethodV;
	private final MethodHandle CallStaticByteMethodA;
	private final MethodHandle CallStaticCharMethod;
	private final MethodHandle CallStaticCharMethodV;
	private final MethodHandle CallStaticCharMethodA;
	private final MethodHandle CallStaticShortMethod;
	private final MethodHandle CallStaticShortMethodV;
	private final MethodHandle CallStaticShortMethodA;
	private final MethodHandle CallStaticIntMethod;
	private final MethodHandle CallStaticIntMethodV;
	private final MethodHandle CallStaticIntMethodA;
	private final MethodHandle CallStaticLongMethod;
	private final MethodHandle CallStaticLongMethodV;
	private final MethodHandle CallStaticLongMethodA;
	private final MethodHandle CallStaticFloatMethod;
	private final MethodHandle CallStaticFloatMethodV;
	private final MethodHandle CallStaticFloatMethodA;
	private final MethodHandle CallStaticDoubleMethod;
	private final MethodHandle CallStaticDoubleMethodV;
	private final MethodHandle CallStaticDoubleMethodA;
	private final MethodHandle CallStaticVoidMethod;
	private final MethodHandle CallStaticVoidMethodV;
	private final MethodHandle CallStaticVoidMethodA;
	private final MethodHandle GetStaticFieldID;
	private final MethodHandle GetStaticObjectField;
	private final MethodHandle GetStaticBooleanField;
	private final MethodHandle GetStaticByteField;
	private final MethodHandle GetStaticCharField;
	private final MethodHandle GetStaticShortField;
	private final MethodHandle GetStaticIntField;
	private final MethodHandle GetStaticLongField;
	private final MethodHandle GetStaticFloatField;
	private final MethodHandle GetStaticDoubleField;
	private final MethodHandle SetStaticObjectField;
	private final MethodHandle SetStaticBooleanField;
	private final MethodHandle SetStaticByteField;
	private final MethodHandle SetStaticCharField;
	private final MethodHandle SetStaticShortField;
	private final MethodHandle SetStaticIntField;
	private final MethodHandle SetStaticLongField;
	private final MethodHandle SetStaticFloatField;
	private final MethodHandle SetStaticDoubleField;
	private final MethodHandle NewString;
	private final MethodHandle GetStringLength;
	private final MethodHandle GetStringChars;
	private final MethodHandle ReleaseStringChars;
	private final MethodHandle NewStringUTF;
	private final MethodHandle GetStringUTFLength;
	private final MethodHandle GetStringUTFChars;
	private final MethodHandle ReleaseStringUTFChars;
	private final MethodHandle GetArrayLength;
	private final MethodHandle NewObjectArray;
	private final MethodHandle GetObjectArrayElement;
	private final MethodHandle SetObjectArrayElement;
	private final MethodHandle NewBooleanArray;
	private final MethodHandle NewByteArray;
	private final MethodHandle NewCharArray;
	private final MethodHandle NewShortArray;
	private final MethodHandle NewIntArray;
	private final MethodHandle NewLongArray;
	private final MethodHandle NewFloatArray;
	private final MethodHandle NewDoubleArray;
	private final MethodHandle GetBooleanArrayElements;
	private final MethodHandle GetByteArrayElements;
	private final MethodHandle GetCharArrayElements;
	private final MethodHandle GetShortArrayElements;
	private final MethodHandle GetIntArrayElements;
	private final MethodHandle GetLongArrayElements;
	private final MethodHandle GetFloatArrayElements;
	private final MethodHandle GetDoubleArrayElements;
	private final MethodHandle ReleaseBooleanArrayElements;
	private final MethodHandle ReleaseByteArrayElements;
	private final MethodHandle ReleaseCharArrayElements;
	private final MethodHandle ReleaseShortArrayElements;
	private final MethodHandle ReleaseIntArrayElements;
	private final MethodHandle ReleaseLongArrayElements;
	private final MethodHandle ReleaseFloatArrayElements;
	private final MethodHandle ReleaseDoubleArrayElements;
	private final MethodHandle GetBooleanArrayRegion;
	private final MethodHandle GetByteArrayRegion;
	private final MethodHandle GetCharArrayRegion;
	private final MethodHandle GetShortArrayRegion;
	private final MethodHandle GetIntArrayRegion;
	private final MethodHandle GetLongArrayRegion;
	private final MethodHandle GetFloatArrayRegion;
	private final MethodHandle GetDoubleArrayRegion;
	private final MethodHandle SetBooleanArrayRegion;
	private final MethodHandle SetByteArrayRegion;
	private final MethodHandle SetCharArrayRegion;
	private final MethodHandle SetShortArrayRegion;
	private final MethodHandle SetIntArrayRegion;
	private final MethodHandle SetLongArrayRegion;
	private final MethodHandle SetFloatArrayRegion;
	private final MethodHandle SetDoubleArrayRegion;
	private final MethodHandle RegisterNatives;
	private final MethodHandle UnregisterNatives;
	private final MethodHandle MonitorEnter;
	private final MethodHandle MonitorExit;
	private final MethodHandle GetJavaVM;
	private final MethodHandle GetStringRegion;
	private final MethodHandle GetStringUTFRegion;
	private final MethodHandle GetPrimitiveArrayCritical;
	private final MethodHandle ReleasePrimitiveArrayCritical;
	private final MethodHandle GetStringCritical;
	private final MethodHandle ReleaseStringCritical;
	private final MethodHandle NewWeakGlobalRef;
	private final MethodHandle DeleteWeakGlobalRef;
	private final MethodHandle ExceptionCheck;
	private final MethodHandle NewDirectByteBuffer;
	private final MethodHandle GetDirectBufferAddress;
	private final MethodHandle GetDirectBufferCapacity;
	private final MethodHandle GetObjectRefType;
	private final MethodHandle GetModule;
	private final MethodHandle IsVirtualThread;
	private final MethodHandle GetStringUTFLengthAsLong;

	public jni_native_interface(long JNINativeInterface_addr)
	{
		this.JNINativeInterface_base = JNINativeInterface_.new object(JNINativeInterface_addr);
		this.GetVersion = JNINativeInterface_base.callable("GetVersion");
		this.DefineClass = JNINativeInterface_base.callable("DefineClass");
		this.FindClass = JNINativeInterface_base.callable("FindClass");
		this.FromReflectedMethod = JNINativeInterface_base.callable("FromReflectedMethod");
		this.FromReflectedField = JNINativeInterface_base.callable("FromReflectedField");
		this.ToReflectedMethod = JNINativeInterface_base.callable("ToReflectedMethod");
		this.GetSuperclass = JNINativeInterface_base.callable("GetSuperclass");
		this.IsAssignableFrom = JNINativeInterface_base.callable("IsAssignableFrom");
		this.ToReflectedField = JNINativeInterface_base.callable("ToReflectedField");
		this.Throw = JNINativeInterface_base.callable("Throw");
		this.ThrowNew = JNINativeInterface_base.callable("ThrowNew");
		this.ExceptionOccurred = JNINativeInterface_base.callable("ExceptionOccurred");
		this.ExceptionDescribe = JNINativeInterface_base.callable("ExceptionDescribe");
		this.ExceptionClear = JNINativeInterface_base.callable("ExceptionClear");
		this.FatalError = JNINativeInterface_base.callable("FatalError");
		this.PushLocalFrame = JNINativeInterface_base.callable("PushLocalFrame");
		this.PopLocalFrame = JNINativeInterface_base.callable("PopLocalFrame");
		this.NewGlobalRef = JNINativeInterface_base.callable("NewGlobalRef");
		this.DeleteGlobalRef = JNINativeInterface_base.callable("DeleteGlobalRef");
		this.DeleteLocalRef = JNINativeInterface_base.callable("DeleteLocalRef");
		this.IsSameObject = JNINativeInterface_base.callable("IsSameObject");
		this.NewLocalRef = JNINativeInterface_base.callable("NewLocalRef");
		this.EnsureLocalCapacity = JNINativeInterface_base.callable("EnsureLocalCapacity");
		this.AllocObject = JNINativeInterface_base.callable("AllocObject");
		this.NewObject = JNINativeInterface_base.callable("NewObject");
		this.NewObjectV = JNINativeInterface_base.callable("NewObjectV");
		this.NewObjectA = JNINativeInterface_base.callable("NewObjectA");
		this.GetObjectClass = JNINativeInterface_base.callable("GetObjectClass");
		this.IsInstanceOf = JNINativeInterface_base.callable("IsInstanceOf");
		this.GetMethodID = JNINativeInterface_base.callable("GetMethodID");
		this.CallObjectMethod = JNINativeInterface_base.callable("CallObjectMethod");
		this.CallObjectMethodV = JNINativeInterface_base.callable("CallObjectMethodV");
		this.CallObjectMethodA = JNINativeInterface_base.callable("CallObjectMethodA");
		this.CallBooleanMethod = JNINativeInterface_base.callable("CallBooleanMethod");
		this.CallBooleanMethodV = JNINativeInterface_base.callable("CallBooleanMethodV");
		this.CallBooleanMethodA = JNINativeInterface_base.callable("CallBooleanMethodA");
		this.CallByteMethod = JNINativeInterface_base.callable("CallByteMethod");
		this.CallByteMethodV = JNINativeInterface_base.callable("CallByteMethodV");
		this.CallByteMethodA = JNINativeInterface_base.callable("CallByteMethodA");
		this.CallCharMethod = JNINativeInterface_base.callable("CallCharMethod");
		this.CallCharMethodV = JNINativeInterface_base.callable("CallCharMethodV");
		this.CallCharMethodA = JNINativeInterface_base.callable("CallCharMethodA");
		this.CallShortMethod = JNINativeInterface_base.callable("CallShortMethod");
		this.CallShortMethodV = JNINativeInterface_base.callable("CallShortMethodV");
		this.CallShortMethodA = JNINativeInterface_base.callable("CallShortMethodA");
		this.CallIntMethod = JNINativeInterface_base.callable("CallIntMethod");
		this.CallIntMethodV = JNINativeInterface_base.callable("CallIntMethodV");
		this.CallIntMethodA = JNINativeInterface_base.callable("CallIntMethodA");
		this.CallLongMethod = JNINativeInterface_base.callable("CallLongMethod");
		this.CallLongMethodV = JNINativeInterface_base.callable("CallLongMethodV");
		this.CallLongMethodA = JNINativeInterface_base.callable("CallLongMethodA");
		this.CallFloatMethod = JNINativeInterface_base.callable("CallFloatMethod");
		this.CallFloatMethodV = JNINativeInterface_base.callable("CallFloatMethodV");
		this.CallFloatMethodA = JNINativeInterface_base.callable("CallFloatMethodA");
		this.CallDoubleMethod = JNINativeInterface_base.callable("CallDoubleMethod");
		this.CallDoubleMethodV = JNINativeInterface_base.callable("CallDoubleMethodV");
		this.CallDoubleMethodA = JNINativeInterface_base.callable("CallDoubleMethodA");
		this.CallVoidMethod = JNINativeInterface_base.callable("CallVoidMethod");
		this.CallVoidMethodV = JNINativeInterface_base.callable("CallVoidMethodV");
		this.CallVoidMethodA = JNINativeInterface_base.callable("CallVoidMethodA");
		this.CallNonvirtualObjectMethod = JNINativeInterface_base.callable("CallNonvirtualObjectMethod");
		this.CallNonvirtualObjectMethodV = JNINativeInterface_base.callable("CallNonvirtualObjectMethodV");
		this.CallNonvirtualObjectMethodA = JNINativeInterface_base.callable("CallNonvirtualObjectMethodA");
		this.CallNonvirtualBooleanMethod = JNINativeInterface_base.callable("CallNonvirtualBooleanMethod");
		this.CallNonvirtualBooleanMethodV = JNINativeInterface_base.callable("CallNonvirtualBooleanMethodV");
		this.CallNonvirtualBooleanMethodA = JNINativeInterface_base.callable("CallNonvirtualBooleanMethodA");
		this.CallNonvirtualByteMethod = JNINativeInterface_base.callable("CallNonvirtualByteMethod");
		this.CallNonvirtualByteMethodV = JNINativeInterface_base.callable("CallNonvirtualByteMethodV");
		this.CallNonvirtualByteMethodA = JNINativeInterface_base.callable("CallNonvirtualByteMethodA");
		this.CallNonvirtualCharMethod = JNINativeInterface_base.callable("CallNonvirtualCharMethod");
		this.CallNonvirtualCharMethodV = JNINativeInterface_base.callable("CallNonvirtualCharMethodV");
		this.CallNonvirtualCharMethodA = JNINativeInterface_base.callable("CallNonvirtualCharMethodA");
		this.CallNonvirtualShortMethod = JNINativeInterface_base.callable("CallNonvirtualShortMethod");
		this.CallNonvirtualShortMethodV = JNINativeInterface_base.callable("CallNonvirtualShortMethodV");
		this.CallNonvirtualShortMethodA = JNINativeInterface_base.callable("CallNonvirtualShortMethodA");
		this.CallNonvirtualIntMethod = JNINativeInterface_base.callable("CallNonvirtualIntMethod");
		this.CallNonvirtualIntMethodV = JNINativeInterface_base.callable("CallNonvirtualIntMethodV");
		this.CallNonvirtualIntMethodA = JNINativeInterface_base.callable("CallNonvirtualIntMethodA");
		this.CallNonvirtualLongMethod = JNINativeInterface_base.callable("CallNonvirtualLongMethod");
		this.CallNonvirtualLongMethodV = JNINativeInterface_base.callable("CallNonvirtualLongMethodV");
		this.CallNonvirtualLongMethodA = JNINativeInterface_base.callable("CallNonvirtualLongMethodA");
		this.CallNonvirtualFloatMethod = JNINativeInterface_base.callable("CallNonvirtualFloatMethod");
		this.CallNonvirtualFloatMethodV = JNINativeInterface_base.callable("CallNonvirtualFloatMethodV");
		this.CallNonvirtualFloatMethodA = JNINativeInterface_base.callable("CallNonvirtualFloatMethodA");
		this.CallNonvirtualDoubleMethod = JNINativeInterface_base.callable("CallNonvirtualDoubleMethod");
		this.CallNonvirtualDoubleMethodV = JNINativeInterface_base.callable("CallNonvirtualDoubleMethodV");
		this.CallNonvirtualDoubleMethodA = JNINativeInterface_base.callable("CallNonvirtualDoubleMethodA");
		this.CallNonvirtualVoidMethod = JNINativeInterface_base.callable("CallNonvirtualVoidMethod");
		this.CallNonvirtualVoidMethodV = JNINativeInterface_base.callable("CallNonvirtualVoidMethodV");
		this.CallNonvirtualVoidMethodA = JNINativeInterface_base.callable("CallNonvirtualVoidMethodA");
		this.GetFieldID = JNINativeInterface_base.callable("GetFieldID");
		this.GetObjectField = JNINativeInterface_base.callable("GetObjectField");
		this.GetBooleanField = JNINativeInterface_base.callable("GetBooleanField");
		this.GetByteField = JNINativeInterface_base.callable("GetByteField");
		this.GetCharField = JNINativeInterface_base.callable("GetCharField");
		this.GetShortField = JNINativeInterface_base.callable("GetShortField");
		this.GetIntField = JNINativeInterface_base.callable("GetIntField");
		this.GetLongField = JNINativeInterface_base.callable("GetLongField");
		this.GetFloatField = JNINativeInterface_base.callable("GetFloatField");
		this.GetDoubleField = JNINativeInterface_base.callable("GetDoubleField");
		this.SetObjectField = JNINativeInterface_base.callable("SetObjectField");
		this.SetBooleanField = JNINativeInterface_base.callable("SetBooleanField");
		this.SetByteField = JNINativeInterface_base.callable("SetByteField");
		this.SetCharField = JNINativeInterface_base.callable("SetCharField");
		this.SetShortField = JNINativeInterface_base.callable("SetShortField");
		this.SetIntField = JNINativeInterface_base.callable("SetIntField");
		this.SetLongField = JNINativeInterface_base.callable("SetLongField");
		this.SetFloatField = JNINativeInterface_base.callable("SetFloatField");
		this.SetDoubleField = JNINativeInterface_base.callable("SetDoubleField");
		this.GetStaticMethodID = JNINativeInterface_base.callable("GetStaticMethodID");
		this.CallStaticObjectMethod = JNINativeInterface_base.callable("CallStaticObjectMethod");
		this.CallStaticObjectMethodV = JNINativeInterface_base.callable("CallStaticObjectMethodV");
		this.CallStaticObjectMethodA = JNINativeInterface_base.callable("CallStaticObjectMethodA");
		this.CallStaticBooleanMethod = JNINativeInterface_base.callable("CallStaticBooleanMethod");
		this.CallStaticBooleanMethodV = JNINativeInterface_base.callable("CallStaticBooleanMethodV");
		this.CallStaticBooleanMethodA = JNINativeInterface_base.callable("CallStaticBooleanMethodA");
		this.CallStaticByteMethod = JNINativeInterface_base.callable("CallStaticByteMethod");
		this.CallStaticByteMethodV = JNINativeInterface_base.callable("CallStaticByteMethodV");
		this.CallStaticByteMethodA = JNINativeInterface_base.callable("CallStaticByteMethodA");
		this.CallStaticCharMethod = JNINativeInterface_base.callable("CallStaticCharMethod");
		this.CallStaticCharMethodV = JNINativeInterface_base.callable("CallStaticCharMethodV");
		this.CallStaticCharMethodA = JNINativeInterface_base.callable("CallStaticCharMethodA");
		this.CallStaticShortMethod = JNINativeInterface_base.callable("CallStaticShortMethod");
		this.CallStaticShortMethodV = JNINativeInterface_base.callable("CallStaticShortMethodV");
		this.CallStaticShortMethodA = JNINativeInterface_base.callable("CallStaticShortMethodA");
		this.CallStaticIntMethod = JNINativeInterface_base.callable("CallStaticIntMethod");
		this.CallStaticIntMethodV = JNINativeInterface_base.callable("CallStaticIntMethodV");
		this.CallStaticIntMethodA = JNINativeInterface_base.callable("CallStaticIntMethodA");
		this.CallStaticLongMethod = JNINativeInterface_base.callable("CallStaticLongMethod");
		this.CallStaticLongMethodV = JNINativeInterface_base.callable("CallStaticLongMethodV");
		this.CallStaticLongMethodA = JNINativeInterface_base.callable("CallStaticLongMethodA");
		this.CallStaticFloatMethod = JNINativeInterface_base.callable("CallStaticFloatMethod");
		this.CallStaticFloatMethodV = JNINativeInterface_base.callable("CallStaticFloatMethodV");
		this.CallStaticFloatMethodA = JNINativeInterface_base.callable("CallStaticFloatMethodA");
		this.CallStaticDoubleMethod = JNINativeInterface_base.callable("CallStaticDoubleMethod");
		this.CallStaticDoubleMethodV = JNINativeInterface_base.callable("CallStaticDoubleMethodV");
		this.CallStaticDoubleMethodA = JNINativeInterface_base.callable("CallStaticDoubleMethodA");
		this.CallStaticVoidMethod = JNINativeInterface_base.callable("CallStaticVoidMethod");
		this.CallStaticVoidMethodV = JNINativeInterface_base.callable("CallStaticVoidMethodV");
		this.CallStaticVoidMethodA = JNINativeInterface_base.callable("CallStaticVoidMethodA");
		this.GetStaticFieldID = JNINativeInterface_base.callable("GetStaticFieldID");
		this.GetStaticObjectField = JNINativeInterface_base.callable("GetStaticObjectField");
		this.GetStaticBooleanField = JNINativeInterface_base.callable("GetStaticBooleanField");
		this.GetStaticByteField = JNINativeInterface_base.callable("GetStaticByteField");
		this.GetStaticCharField = JNINativeInterface_base.callable("GetStaticCharField");
		this.GetStaticShortField = JNINativeInterface_base.callable("GetStaticShortField");
		this.GetStaticIntField = JNINativeInterface_base.callable("GetStaticIntField");
		this.GetStaticLongField = JNINativeInterface_base.callable("GetStaticLongField");
		this.GetStaticFloatField = JNINativeInterface_base.callable("GetStaticFloatField");
		this.GetStaticDoubleField = JNINativeInterface_base.callable("GetStaticDoubleField");
		this.SetStaticObjectField = JNINativeInterface_base.callable("SetStaticObjectField");
		this.SetStaticBooleanField = JNINativeInterface_base.callable("SetStaticBooleanField");
		this.SetStaticByteField = JNINativeInterface_base.callable("SetStaticByteField");
		this.SetStaticCharField = JNINativeInterface_base.callable("SetStaticCharField");
		this.SetStaticShortField = JNINativeInterface_base.callable("SetStaticShortField");
		this.SetStaticIntField = JNINativeInterface_base.callable("SetStaticIntField");
		this.SetStaticLongField = JNINativeInterface_base.callable("SetStaticLongField");
		this.SetStaticFloatField = JNINativeInterface_base.callable("SetStaticFloatField");
		this.SetStaticDoubleField = JNINativeInterface_base.callable("SetStaticDoubleField");
		this.NewString = JNINativeInterface_base.callable("NewString");
		this.GetStringLength = JNINativeInterface_base.callable("GetStringLength");
		this.GetStringChars = JNINativeInterface_base.callable("GetStringChars");
		this.ReleaseStringChars = JNINativeInterface_base.callable("ReleaseStringChars");
		this.NewStringUTF = JNINativeInterface_base.callable("NewStringUTF");
		this.GetStringUTFLength = JNINativeInterface_base.callable("GetStringUTFLength");
		this.GetStringUTFChars = JNINativeInterface_base.callable("GetStringUTFChars");
		this.ReleaseStringUTFChars = JNINativeInterface_base.callable("ReleaseStringUTFChars");
		this.GetArrayLength = JNINativeInterface_base.callable("GetArrayLength");
		this.NewObjectArray = JNINativeInterface_base.callable("NewObjectArray");
		this.GetObjectArrayElement = JNINativeInterface_base.callable("GetObjectArrayElement");
		this.SetObjectArrayElement = JNINativeInterface_base.callable("SetObjectArrayElement");
		this.NewBooleanArray = JNINativeInterface_base.callable("NewBooleanArray");
		this.NewByteArray = JNINativeInterface_base.callable("NewByteArray");
		this.NewCharArray = JNINativeInterface_base.callable("NewCharArray");
		this.NewShortArray = JNINativeInterface_base.callable("NewShortArray");
		this.NewIntArray = JNINativeInterface_base.callable("NewIntArray");
		this.NewLongArray = JNINativeInterface_base.callable("NewLongArray");
		this.NewFloatArray = JNINativeInterface_base.callable("NewFloatArray");
		this.NewDoubleArray = JNINativeInterface_base.callable("NewDoubleArray");
		this.GetBooleanArrayElements = JNINativeInterface_base.callable("GetBooleanArrayElements");
		this.GetByteArrayElements = JNINativeInterface_base.callable("GetByteArrayElements");
		this.GetCharArrayElements = JNINativeInterface_base.callable("GetCharArrayElements");
		this.GetShortArrayElements = JNINativeInterface_base.callable("GetShortArrayElements");
		this.GetIntArrayElements = JNINativeInterface_base.callable("GetIntArrayElements");
		this.GetLongArrayElements = JNINativeInterface_base.callable("GetLongArrayElements");
		this.GetFloatArrayElements = JNINativeInterface_base.callable("GetFloatArrayElements");
		this.GetDoubleArrayElements = JNINativeInterface_base.callable("GetDoubleArrayElements");
		this.ReleaseBooleanArrayElements = JNINativeInterface_base.callable("ReleaseBooleanArrayElements");
		this.ReleaseByteArrayElements = JNINativeInterface_base.callable("ReleaseByteArrayElements");
		this.ReleaseCharArrayElements = JNINativeInterface_base.callable("ReleaseCharArrayElements");
		this.ReleaseShortArrayElements = JNINativeInterface_base.callable("ReleaseShortArrayElements");
		this.ReleaseIntArrayElements = JNINativeInterface_base.callable("ReleaseIntArrayElements");
		this.ReleaseLongArrayElements = JNINativeInterface_base.callable("ReleaseLongArrayElements");
		this.ReleaseFloatArrayElements = JNINativeInterface_base.callable("ReleaseFloatArrayElements");
		this.ReleaseDoubleArrayElements = JNINativeInterface_base.callable("ReleaseDoubleArrayElements");
		this.GetBooleanArrayRegion = JNINativeInterface_base.callable("GetBooleanArrayRegion");
		this.GetByteArrayRegion = JNINativeInterface_base.callable("GetByteArrayRegion");
		this.GetCharArrayRegion = JNINativeInterface_base.callable("GetCharArrayRegion");
		this.GetShortArrayRegion = JNINativeInterface_base.callable("GetShortArrayRegion");
		this.GetIntArrayRegion = JNINativeInterface_base.callable("GetIntArrayRegion");
		this.GetLongArrayRegion = JNINativeInterface_base.callable("GetLongArrayRegion");
		this.GetFloatArrayRegion = JNINativeInterface_base.callable("GetFloatArrayRegion");
		this.GetDoubleArrayRegion = JNINativeInterface_base.callable("GetDoubleArrayRegion");
		this.SetBooleanArrayRegion = JNINativeInterface_base.callable("SetBooleanArrayRegion");
		this.SetByteArrayRegion = JNINativeInterface_base.callable("SetByteArrayRegion");
		this.SetCharArrayRegion = JNINativeInterface_base.callable("SetCharArrayRegion");
		this.SetShortArrayRegion = JNINativeInterface_base.callable("SetShortArrayRegion");
		this.SetIntArrayRegion = JNINativeInterface_base.callable("SetIntArrayRegion");
		this.SetLongArrayRegion = JNINativeInterface_base.callable("SetLongArrayRegion");
		this.SetFloatArrayRegion = JNINativeInterface_base.callable("SetFloatArrayRegion");
		this.SetDoubleArrayRegion = JNINativeInterface_base.callable("SetDoubleArrayRegion");
		this.RegisterNatives = JNINativeInterface_base.callable("RegisterNatives");
		this.UnregisterNatives = JNINativeInterface_base.callable("UnregisterNatives");
		this.MonitorEnter = JNINativeInterface_base.callable("MonitorEnter");
		this.MonitorExit = JNINativeInterface_base.callable("MonitorExit");
		this.GetJavaVM = JNINativeInterface_base.callable("GetJavaVM");
		this.GetStringRegion = JNINativeInterface_base.callable("GetStringRegion");
		this.GetStringUTFRegion = JNINativeInterface_base.callable("GetStringUTFRegion");
		this.GetPrimitiveArrayCritical = JNINativeInterface_base.callable("GetPrimitiveArrayCritical");
		this.ReleasePrimitiveArrayCritical = JNINativeInterface_base.callable("ReleasePrimitiveArrayCritical");
		this.GetStringCritical = JNINativeInterface_base.callable("GetStringCritical");
		this.ReleaseStringCritical = JNINativeInterface_base.callable("ReleaseStringCritical");
		this.NewWeakGlobalRef = JNINativeInterface_base.callable("NewWeakGlobalRef");
		this.DeleteWeakGlobalRef = JNINativeInterface_base.callable("DeleteWeakGlobalRef");
		this.ExceptionCheck = JNINativeInterface_base.callable("ExceptionCheck");
		this.NewDirectByteBuffer = JNINativeInterface_base.callable("NewDirectByteBuffer");
		this.GetDirectBufferAddress = JNINativeInterface_base.callable("GetDirectBufferAddress");
		this.GetDirectBufferCapacity = JNINativeInterface_base.callable("GetDirectBufferCapacity");
		this.GetObjectRefType = JNINativeInterface_base.callable("GetObjectRefType");
		this.GetModule = JNINativeInterface_base.callable("GetModule");
		this.IsVirtualThread = JNINativeInterface_base.callable("IsVirtualThread");
		this.GetStringUTFLengthAsLong = JNINativeInterface_base.callable("GetStringUTFLengthAsLong");
	}

	public final long get_string_utf_chars(String jstr, boolean is_copy)
	{
		try (pointer pis_copy = memory.malloc(cxx_type.jboolean).auto();)
		{
			pis_copy.dereference_assign(is_copy);
			return (long) GetStringUTFChars.invokeExact(JNINativeInterface_base.address(), java_type.oop_of(jstr), pis_copy.address());
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNINativeInterface_::GetStringUTFChars() failed", ex);
		}
	}

	public final void release_string_utf_chars(String jstr, long cstr)
	{
		try
		{
			ReleaseStringUTFChars.invokeExact(JNINativeInterface_base.address(), jstr, cstr);
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNINativeInterface_::ReleaseStringUTFChars() failed", ex);
		}
	}

	/**
	 * 获取当前实际的JNI版本
	 * 
	 * @return
	 */
	public final int get_version()
	{
		try
		{
			return (int) GetVersion.invokeExact(JNINativeInterface_base.address());
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNINativeInterface_::GetVersion() failed", ex);
		}
	}

	public final long find_class(String bin_name)
	{
		try (pointer cstr = memory.c_str(bin_name).auto())
		{
			return (long) FindClass.invokeExact(JNINativeInterface_base.address(), cstr.address());
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNINativeInterface_::FindClass() failed", ex);
		}
	}

	public final void fatal_error(String msg)
	{
		try (pointer cstr = memory.c_str(msg).auto())
		{
			FatalError.invokeExact(JNINativeInterface_base.address(), cstr.address());
		}
		catch (Throwable ex)
		{
			throw new java.lang.InternalError("call JNINativeInterface_::FatalError() failed", ex);
		}
	}
}
