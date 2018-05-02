struct LONG_TIME {
            UNUM32 TimeHigh;
            UNUM32 TimeLow;
      } longTime;
 
UNUM32 g_PreviousClockTime = 0;
 
 
 
longTime.TimeLow = 0;          // init before longClock called
 
 
LONG_TIME longClock(UNUM32 Timestamp) {
            if (((Timestamp & 0x80000000) == 0) && ((g_PreviousClockTime & 0x80000000) == 1))
                  longTime.TimeHigh++;
 
            g_PreviousClockTime = Timestamp;
            longtime.TimeLow = Timestamp;  
     
      return longTime;  
}