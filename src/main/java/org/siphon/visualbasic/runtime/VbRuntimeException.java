/*******************************************************************************
 * Copyright (C) 2017 Inshua<inshua@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.siphon.visualbasic.runtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.siphon.visualbasic.SourceLocation;

/*
 * 

 */
public class VbRuntimeException extends Exception {
	
	public final static int GOSUB_WITHOUT_RETURN=3;

	public final static int INVALID_PROCEDURE_CALL=5;

	public final static int OVERFLOW=6;

	public final static int OUT_OF_STORAGE=7;

	public final static int INDEX_OUT_OF_BOUNDS=9;

	public final static int THIS_ARRAY_IS_FIXED_OR_TEMPORARILY_LOCKED=10;

	public final static int DIVIDE_BY_ZERO=11;

	public final static int TYPE_MISMATCH=13;

	public final static int INSUFFICIENT_STRING_SPACE=14;

	public final static int EXPRESSION_IS_TOO_COMPLICATED=16;

	public final static int CANNOT_COMPLETE_THE_REQUESTED_OPERATION=17;

	public final static int USER_INTERRUPTION=18;

	public final static int NO_RECOVERY_ERROR=20;

	public final static int INSUFFICIENT_STACK_SPACE=28;

	public final static int NO_SUBROUTINE_FUNCTION__OR_ATTRIBUTE_DEFINED=35;

	public final static int TOO_MANY_CLIENTS_FOR_DLL_APPLICATIONS=47;

	public final static int AN_ERROR_OCCURRED_WHILE_LOADING_THE_DLL=48;

	public final static int DLL_CALL_SPECIFICATION_ERROR=49;

	public final static int INTERNAL_ERROR=51;

	public final static int WRONG_FILE_NAME_OR_NUMBER=52;

	public final static int FILE_NOT_FOUND=53;

	public final static int WRONG_FILE_MODE=54;

	public final static int FILE_IS_OPEN=55;

	public final static int IO_DEVICE_ERROR=57;

	public final static int THE_FILE_ALREADY_EXISTS=58;

	public final static int WRONG_LENGTH_OF_RECORD=59;

	public final static int THE_DISK_IS_FULL=61;

	public final static int INPUT_HAS_EXCEEDED_THE_END_OF_THE_FILE=62;

	public final static int THE_NUMBER_OF_RECORDS_IS_WRONG=63;

	public final static int TOO_MANY_FILES=67;

	public final static int DEVICE_NOT_AVAILABLE=68;

	public final static int NO_ACCESS=70;

	public final static int DISK_IS_NOT_READY=71;

	public final static int CANNOT_BE_RENAMED_WITH_ANOTHER_DISK_UNIT=74;

	public final static int PATH_FILE_ACCESS_ERROR=75;

	public final static int PATH_NOT_FOUND=76;

	public final static int OBJECT_VARIABLE_OR_WITH_BLOCK_VARIABLE_HAS_NOT_BEEN_SET_YET=91;

	public final static int FOR_LOOP_IS_NOT_INITIALIZED=92;

	public final static int INVALID_MODE_STRING=93;

	public final static int NULL_USE_IS_INVALID=94;

	public final static int CANNOT_CALL_FRIEND_PROCEDURE_ON_AN_OBJECT_THIS_OBJECT_IS_NOT_AN_INSTANCE_OF_A_DEFINED_CLASS=97;

	public final static int SYSTEM_DLL_CANNOT_BE_LOADED=298;

	public final static int CHARACTER_DEVICE_NAME_CANNOT_BE_USED_IN_THE_SPECIFIED_FILE=320;

	public final static int INVALID_FILE_FORMAT=321;

	public final static int CANNOT_CREATE_NECESSARY_TEMPORARY_FILES=322;

	public final static int INVALID_FORMAT_IN_SOURCE_FILE=325;

	public final static int NO_NAMED_DATA_VALUE_FOUND=327;

	public final static int ILLEGAL_PARAMETER___CAN_NOT_BE_WRITTEN_TO_THE_ARRAY=328;

	public final static int CANNOT_ACCESS_SYSTEM_REGISTRY=335;

	public final static int ACTIVEX_COMPONENTS_ARE_NOT_PROPERLY_REGISTERED=336;

	public final static int ACTIVEX_COMPONENT_NOT_FOUND=337;

	public final static int ACTIVEX_COMPONENTS_DO_NOT_WORK_CORRECTLY=338;
	
	public final static int CONTROL_ARRAY_ELEMENT_DOES_NOT_EXIST=340;

	public final static int OBJECT_HAS_BEEN_LOADED=360;

	public final static int CANNOT_LOAD_OR_UNLOAD_THE_OBJECT=361;

	public final static int THE_SPECIFIED_ACTIVEX_CONTROL_WAS_NOT_FOUND=363;

	public final static int OBJECT_NOT_UNLOADED=364;

	public final static int CANNOT_BE_UNINSTALLED_IN_THIS_CONTEXT=365;

	public final static int SPECIFIED_FILE_OBSOLETE_THIS_PROGRAM_REQUIRES_A_NEWER_VERSION=368;

	public final static int THE_SPECIFIED_OBJECT_CANNOT_BE_USED_AS_THE_OWNER_FORM_FOR_DISPLAY=371;

	public final static int INVALID_ATTRIBUTE_VALUE=380;

	public final static int INVALID_PROPERTY_ARRAY_INDEX=381;

	public final static int PROPERTY_SETTINGS_CANNOT_BE_COMPLETED_AT_RUNTIME=382;

	public final static int PROPERTY_SETTINGS_CANNOT_BE_USED_FOR_READ_ONLY_PROPERTIES=383;

	public final static int NEED_ATTRIBUTE_ARRAY_INDEX=385;

	public final static int PROPERTY_SETTINGS_ARE_NOT_ALLOWED=387;

	public final static int ATTRIBUTE_ACQUISITION_CANNOT_BE_DONE_AT_RUNTIME=393;

	public final static int ATTRIBUTE_ACQUISITION_CANNOT_BE_USED_TO_WRITE_ONLY_ATTRIBUTES=394;

	public final static int THE_FORM_HAS_BEEN_DISPLAYED___CAN_NOT_BE_DISPLAYED_AS_A_MODAL_FORM=400;

	public final static int CODE_MUST_FIRST_CLOSE_THE_TOP_MODE_FORM=402;

	public final static int ALLOW_NEGATIVE_OBJECTS=419;

	public final static int PROPERTY_NOT_FOUND=422;

	public final static int PROPERTY_NOT_FOUND或方法=423;

	public final static int REQUEST_OBJECT=424;

	public final static int INVALID_OBJECT_USE=425;

	public final static int ACTIVEX_COMPONENT_CANNOT_CREATE_AN_OBJECT_OR_RETURN_A_REFERENCE_TO_THIS_OBJECT=429;

	public final static int CLASS_DOES_NOT_SUPPORT_AUTOMATIC_OPERATION=430;

	public final static int FILE_OR_CLASS_NAME_NOT_FOUND_DURING_AUTOMATIC_OPERATION=432;

	public final static int OBJECT_DOES_NOT_SUPPORT_THIS_PROPERTY_OR_METHOD=438;

	public final static int AUTOMATIC_OPERATION_ERROR=440;

	public final static int REMOTE_PROCESSING_CONNECTED_TO_A_TYPE_LIBRARY_OR_OBJECT_LIBRARY_HAS_BEEN_LOST=442;

	public final static int THE_AUTOMATION_OBJECT_HAS_NO_DEFAULT_VALUE=443;

	public final static int OBJECT_DOES_NOT_SUPPORT_THIS_ACTION=445;

	public final static int OBJECT_DOES_NOT_SUPPORT_SPECIFIED_PARAMETERS=446;

	public final static int OBJECT_DOES_NOT_SUPPORT_CURRENT_LOCATION_SETTINGS=447;

	public final static int CANNOT_FIND_THE_SPECIFIED_PARAMETER=448;

	public final static int PARAMETER_NON_SELECTIVE_OR_INVALID_PROPERTY_SETTINGS=449;

	public final static int INCORRECT_NUMBER_OF_PARAMETERS_OR_INVALID_PROPERTY_SETTINGS=450;

	public final static int OBJECT_IS_NOT_A_COLLECTION_OBJECT=451;

	public final static int INVALID_ORDINAL=452;

	public final static int CANNOT_FIND_THE_SPECIFIED_DLL_FUNCTION=453;

	public final static int SOURCE_CODE_NOT_FOUND=454;

	public final static int CODE_SOURCE_LOCK_ERROR=455;

	public final static int THIS_KEY_IS_ALREADY_ASSOCIATED_WITH_AN_ELEMENT_IN_THE_COLLECTION_OBJECT=457;

	public final static int THE_TYPE_OF_VARIABLE_USED_IS_NOT_SUPPORTED_BY_VISUAL_BASIC=458;

	public final static int THIS_COMPONENT_DOES_NOT_SUPPORT_EVENTS=459;

	public final static int INVALID_CLIPBOARD_FORMAT=460;

	public final static int METHOD_OR_DATA_MEMBER_NOT_FOUND=461;
	public final static int THE_REMOTE_SERVER_MACHINE_DOES_NOT_EXIST_OR_IS_UNAVAILABLE=462;

	public final static int CLASS_NOT_REGISTERED_ON_LOCAL_MACHINE=463;

	public final static int CANNOT_CREATE_AUTOREDRAW_IMAGE=480;

	public final static int INVALID_IMAGE=481;

	public final static int PRINTER_ERROR=482;

	public final static int THE_PRINT_DRIVER_DOES_NOT_SUPPORT_THE_SPECIFIED_ATTRIBUTE=483;

	public final static int ERROR_GETTING_PRINTER_INFORMATION_FROM_THE_SYSTEM___MAKE_SURE_THE_PRINTER_IS_SET_UP_CORRECTLY=484;

	public final static int INVALID_IMAGE_TYPE=485;

	public final static int YOU_CANNOT_PRINT_A_FORM_IMAGE_WITH_THIS_TYPE_OF_PRINTER=486;

	public final static int CANT_EMPTY_THE_CLIPBOARD=520;

	public final static int CANT_OPEN_CLIPBOARD=521;

	public final static int CANNOT_SAVE_FILE_TO_TEMP_DIRECTORY=735;

	public final static int CANT_FIND_THE_TEXT_TO_SEARCH_FOR=744;

	public final static int REPLACE_THE_DATA_TOO_LONG=746;

	public final static int MEMORY_OVERFLOW=31001;

	public final static int NO_OBJECT=31004;

	public final static int CLASS_NOT_SET=31018;

	public final static int CANNOT_ACTIVATE_OBJECT=31027;

	public final static int CANNOT_CREATE_INLINE_OBJECTS=31032;

	public final static int ERROR_SAVING_TO_FILE=31036;

	public final static int ERROR_READING_FROM_FILE=31037;
	
	public final static int NO_MATCHING_RULES=34000;
	
	private final static Map<Integer, String> messages = new HashMap<>();

	
	
	static{
		messages.put(3, "GoSub without return");
		
		
		messages.put(5, "Invalid procedure call");
		
		
		messages.put(6, "overflow");
		
		
		messages.put(7, "Not enough storage");
		
		
		messages.put(9, "Subscript crossing the border");
		
		
		messages.put(10, "This array is fixed or temporarily locked");
		
		
		messages.put(11, "Divide by zero");
		
		
		messages.put(13, "Type mismatch");
		
		
		messages.put(14, "Insufficient string space");
		
		
		messages.put(16, "Expression is too complicated");
		
		
		messages.put(17, "Cannot complete the requested operation");
		
		
		messages.put(18, "User interruption");
		
		
		messages.put(20, "No recovery error");
		
		
		messages.put(28, "Insufficient stack space");
		
		
		messages.put(35, "No subprograms, functions, or properties defined");
		
		
		messages.put(47, "Too many clients for DLL applications");
		
		
		messages.put(48, "An error occurred while loading the DLL");
		
		
		messages.put(49, "DLL call specification error");
		
		
		messages.put(51, "internal error");
		
		
		messages.put(52, "Wrong file name or number");
		
		
		messages.put(53, "File not found");
		
		
		messages.put(54, "Wrong file mode");
		
		
		messages.put(55, "File is open");
		
		
		messages.put(57, "I/O device error");
		
		
		messages.put(58, "The file already exists");
		
		
		messages.put(59, "Wrong length of record");
		
		
		messages.put(61, "The disk is full");
		
		
		messages.put(62, "Input has exceeded the end of the file");
		
		
		messages.put(63, "The number of records is wrong");
		
		
		messages.put(67, "Too many files");
		
		
		messages.put(68, "Device not available");
		
		
		messages.put(70, "No access");
		
		
		messages.put(71, "Disk is not ready");
		
		
		messages.put(74, "Cannot be renamed with another disk unit");
		
		
		messages.put(75, "Path / file access error");
		
		
		messages.put(76, "Path not found");
		
		
		messages.put(91, "The object variable or the With block variable has not been set");
		
		
		messages.put(92, "For loop is not initialized");
		
		
		messages.put(93, "Invalid mode string");
		
		
		messages.put(94, "Null's use is invalid");
		
		
		messages.put(97, "Cannot call Friend procedure on an object, this object is not an instance of a defined class");
		
		
		messages.put(298, "System DLL cannot be loaded");
		
		
		messages.put(320, "Character device name cannot be used in the specified file");
		
		
		messages.put(321, "Invalid file format");
		
		
		messages.put(322, "Cannot create necessary temporary files");
		
		
		messages.put(325, "Invalid format in source file");
		
		
		messages.put(327, "No named data value found");
		
		
		messages.put(328, "Illegal parameter, can't write to array");
		
		
		messages.put(335, "Cannot access system registry");
		
		
		messages.put(336, "ActiveX parts are not registered correctly");
		
		
		messages.put(337, "ActiveX part not found");
		
		
		messages.put(338, "ActiveX parts do not work correctly");
		
		
		messages.put(360, "Object has been loaded");
		
		
		messages.put(361, "Cannot load or unload the object");
		
		
		messages.put(363, "The specified ActiveX control was not found");
		
		
		messages.put(364, "Object not unloaded");
		
		
		messages.put(365, "Cannot be uninstalled in this context");
		
		
		messages.put(368, "The specified file is out of date. The program requires a newer version");
		
		
		messages.put(371, "The specified object cannot be used as the owner form for display");
		
		
		messages.put(380, "Invalid attribute value");
		
		
		messages.put(381, "Invalid property array index");
		
		
		messages.put(382, "Property settings cannot be completed at runtime");
		
		
		messages.put(383, "Property settings cannot be used for read-only properties");
		
		
		messages.put(385, "Need attribute array index");
		
		
		messages.put(387, "Property settings are not allowed");
		
		
		messages.put(393, "Attribute acquisition cannot be done at runtime");
		
		
		messages.put(394, "Attribute acquisition cannot be used to write only attributes");
		
		
		messages.put(400, "The form is already displayed and cannot be displayed as a modal form");
		
		
		messages.put(402, "Code must first close the top mode form");
		
		
		messages.put(419, "Allow negative objects");
		
		
		messages.put(422, "Property not found");
		
		
		messages.put(423, "Property or method not found");
		
		
		messages.put(424, "requires object");
		
		
		messages.put(425, "Invalid object use");
		
		
		messages.put(429, "An ActiveX component cannot create an object or return a reference to this object");
		
		
		messages.put(430, "Class does not support automatic operation");
		
		
		messages.put(432, "File or class name not found during automatic operation");
		
		
		messages.put(438, "Object does not support this property or method");
		
		
		messages.put(440, "Automatic operation error");
		
		
		messages.put(442, "Remote processing connected to a type library or object library has been lost");
		
		
		messages.put(443, "The automation object has no default value");
		
		
		messages.put(445, "Object does not support this action");
		
		
		messages.put(446, "Object does not support specified parameters");
		
		
		messages.put(447, "Object does not support current location settings");
		
		
		messages.put(448, "Cannot find the specified parameter");
		
		
		messages.put(449, "Parameter non-selective or invalid property settings");
		
		
		messages.put(450, "Incorrect number of parameters or invalid property settings");
		
		
		messages.put(451, "Object is not a collection object");
		
		
		messages.put(452, "Invalid ordinal");
		
		
		messages.put(453, "The specified DLL function could not be found");
		
		
		messages.put(454, "Source code not found");
		
		
		messages.put(455, "Code source lock error");
		
		
		messages.put(457, "This key is already associated with an element in the collection object");
		
		
		messages.put(458, "The type of variable used is not supported by Visual Basic");
		
		
		messages.put(459, "This component does not support events");
		
		
		messages.put(460, "Invalid clipboard format");
		
		
		messages.put(461, "Method or data member not found");
		
		messages.put(462, "The remote server machine does not exist or is unavailable");
		
		
		messages.put(463, "Class not registered on local machine");
		
		
		messages.put(480, "Cannot create AutoRedraw image");
		
		
		messages.put(481, "Invalid image");
		
		
		messages.put(482, "Printer error");
		
		
		messages.put(483, "The print driver does not support the specified attribute");
		
		
		messages.put(484, "An error occurred while getting printer information from the system. Make sure the printer is set up correctly");
		
		
		messages.put(485, "Invalid image type");
		
		
		messages.put(486, "You cannot print a form image with this type of printer");
		
		
		messages.put(520, "Can't empty the clipboard");
		
		
		messages.put(521, "Can't open clipboard");
		
		
		messages.put(735, "Cannot save file to TEMP directory");
		
		
		messages.put(744, "Can't find the text to search for");
		
		
		messages.put(746, "Replace the data too long");
		
		
		messages.put(31001, "Memory overflow");
		
		
		messages.put(31004, "No object");
		
		
		messages.put(31018, "Class not set");
		
		
		messages.put(31027, "Cannot activate object");
		
		
		messages.put(31032, "Cannot create inline objects");
		
		
		messages.put(31036, "Error saving to file");
		
		
		messages.put(31037, "Error reading from file");

		messages.put(34000, "No matching rules");
	}
	
	
	public final int code;
	private final SourceLocation sourceLocation;

	private boolean hasVbStackTrace;
	
	public VbRuntimeException(int code, SourceLocation sourceLocation){
		this(code, sourceLocation, null);
	}

	public VbRuntimeException(int code) {
		this(code, SourceLocation.ByInterpreter);
	}

	public VbRuntimeException(int code, SourceLocation sourceLocation, Exception cause) {
		super(StringUtils.defaultIfEmpty(messages.get(code), "unknown error " + code), cause);
		this.code = code;
		this.sourceLocation = sourceLocation;
	}
	
	public VbRuntimeException(int code, Exception cause) {
		this(code, SourceLocation.ByInterpreter, cause);
	}

	public SourceLocation getSourceLocation() {
		return sourceLocation;
	}

	public void setVbStackTrace(StackTraceElement[] stackTrace){
		StackTraceElement[] old = this.getStackTrace();
		StackTraceElement[] items = new StackTraceElement[old.length + stackTrace.length];
		for(int i=0; i<stackTrace.length; i++){
			items[i] = stackTrace[i];
		}
		for(int i =0; i<old.length ; i++){
			items[stackTrace.length + i] = old[i];
		}
		this.setStackTrace(items);
		this.hasVbStackTrace = true;
	}

	public boolean hasVbStackTrace() {
		return hasVbStackTrace;
	}

}
