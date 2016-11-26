package rs.dlogic.wrapper;



public enum E_ERROR_CODES{
	DL_OK (0x00),      
    TIMEOUT_ERROR (0x01),  
    NULL_POINTER  (0x02),  
    PARAMETERS_ERROR (0x03),  
    MEMORY_ALLOCATION_ERROR (0x04),   
    NOT_INITIALIZED (0x05),   
    ALREADY_INITIALIZED (0x06),   
    TIMESTAMP_INVALID (0x07),   
    EVENT_BUSY (0x08),   
    ERR_SPECIFIC (0x1000),   
    CMD_BRAKE_RTE (0x1001),   
    USB_RF_ACK_FAILED (0x1002),   
    NO_RF_PACKET_DATA (0x1003),   
    TRANSFER_WRITING_ERROR (0x1004),   
    EVENT_WAKEUP_BUSY (0x1005),   
    RESOURCE_NOT_ACQUIRED (0x2000),   
    RESOURCE_ALREADY_ACQUIRED (0x2001),   
    RESOURCE_BUSY (0x2002),   
    FILE_OVERSIZE (0x3000),   
    FILE_EMPTY (0x3001),   
    FILE_LOCKED (0x3002),    
    FILE_NOT_FOUND (0x3003),   
    ERR_NO_FILE_NAME (0x3004),   
    ERR_DIR_CAN_NOT_CREATE (0x3005),   
    ERR_DATA (0x4000),   
    ERR_BUFFER_EMPTY (0x4001),   
    ERR_BUFFER_OVERFLOW (0x4002),   
    ERR_CHECKSUM (0x4003),   
    LOG_NOT_CORRECT (0x4004),   
    LIST_ERROR (0x7000),   
    ITEM_IS_ALREADY_IN_LIST (0x7001),   
    ITEM_NOT_IN_LIST (0x7002),   
    ITEM_NOT_VALID (0x7003),   
    NO_DEVICES (0x8000),       
    DEVICE_OPENING_ERROR (0x8001),   
    DEVICE_CAN_NOT_OPEN (0x8002),   
    DEVICE_ALREADY_OPENED (0x8003),   
    DEVICE_NOT_OPENED (0x8004),   
    DEVICE_INDEX_OUT_OF_BOUND (0x8005),   
    DEVICE_CLOSE_ERROR (0x8006),   
    DEVICE_UNKNOWN (0x8007),   
    ERR_COMMAND_RESPONSE (0x9000),   
    CMD_RESPONSE_UNKNOWN_COMMAND (0x9001),   
    CMD_RESPONSE_WRONG_CMD (0x9002),   
    CMD_RESPONSE_COMMAND_FAILED (0x9003),   
    CMD_RESPONSE_UNSUCCESS (0x9004),   
    CMD_RESPONSE_NO_AUTHORIZATION (0x9005),   
    CMD_RESPONSE_SIZE_OVERFLOW (0x9006),   
    CMD_RESPONSE_NO_DATA (0x9007),   
    THREAD_FAILURE (0xA000),   
    ERR_OBJ_NOT_CREATED (0xA001),       
    ERR_CREATE_SEMAPHORE (0xA002),   
    ERR_STATE_MACHINE (0xB000),   
    ERR_SM_IDLE__NO_RESPONSE (0xB001),   
    ERR_SM_COMMAND_IN_PROGRESS (0xB002),   
    ERR_SM_NOT_IDLE (0xB003),   
    ERR_SM_CMD_NOT_STARTED (0xB004),   
    READER_ERRORS_ (0xD000),   
    READER_UID_ERROR (0xD001),   
    READER_LOG_ERROR (0xD002),   
    DL_HAMMING_ERROR (0xE000),   
    DL_HAMMING_NOT_ACK (0xE001),   
    DL_HAMMING_WRONG_ACK (0xE002),   
    DL_HAMMING_WRONG_REPLAY (0xE003),   
    ERROR_SOME_REPLAY_FAULT (0xE004),   
    DL_HAMMING_TERR_TIMEOUT (0xE005),   
    DL_HAMMING_TERR_BAD_FRAME (0xE006),   
    DL_HAMMING_TERR_BAD_SUM (0xE007),   
    DL_HAMMING_TERR_BAD_CODE (0xE008),   
    DL_HAMMING_TERR_TOO_OLD (0xE009),   
    DL_HAMMING_TERR_NOISE (0xE00A),    
    DL_HAMMING_TERR_ERROR_MASK (0xE00B),   
    NO_FTDI_COMM_DEVICES (0xF000),   
    NO_FTDI_COMM_DEVICES_OPENED (0xF001),   
    ERR_FTDI (0xF002),   
    ERR_FTDI_READ  (0xF003),   
    ERR_FTDI_READ_LESS_DATA (0xF004),   
    ERR_FTDI_WRITE (0xF005),   
    ERR_FTDI_WRITE_LESS_DATA (0xF006),   
    DL_FT_ERROR_SET_TIMEOUT (0xF007),   
    DL_FT_ (0xF100),   
    DL_FT_INVALID_HANDLE (0xF101),   
    DL_FT_DEVICE_NOT_FOUND (0xF102),   
    DL_FT_DEVICE_NOT_OPENED (0xF103),   
    DL_FT_IO_ERROR (0xF104),   
    DL_FT_INSUFFICIENT_RESOURCES (0xF105),   
    DL_FT_INVALID_PARAMETER (0xF106),   
    DL_FT_INVALID_BAUD_RATE (0xF107),   
    DL_FT_DEVICE_NOT_OPENED_FOR_ERASE (0xF108),   
    DL_FT_DEVICE_NOT_OPENED_FOR_WRITE (0xF109),   
    DL_FT_FAILED_TO_WRITE_DEVICE (0xF10A),   
    DL_FT_EEPROM_READ_FAILED (0xF10B),   
    DL_FT_EEPROM_WRITE_FAILED (0xF10C),   
    DL_FT_EEPROM_ERASE_FAILED (0xF10D),   
    DL_FT_EEPROM_NOT_PRESENT  (0xF10E),   
    DL_FT_EEPROM_NOT_PROGRAMMED (0xF10F),   
    DL_FT_INVALID_ARGS (0xF110),   
    DL_FT_NOT_SUPPORTED (0xF111),   
    DL_FT_OTHER_ERROR (0xF112),   
    DL_FT_DEVICE_LIST_NOT_READY (0xF113),   
    NOT_IMPLEMENTED (0xFFFFFFFE),      
    UNKNOWN_ERROR (0xFFFFFFFF),   
    MAX_DL_STATUS (0xFFFFFFFF),   
    LAST_ERROR (0xFFFFFFFF);   
   	
   	 private int value;

     private E_ERROR_CODES(int value) {
                this.value = value;
        }
        
     public int value(){
            return value;
        }  	
 }