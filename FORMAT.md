# Message Format

CalendarIQ basically sends just a list of values to target apps. These are as follows:

| Value                  | Type  | Notes                                                        |
| ---------------------- | ----- | ------------------------------------------------------------ |
| Timestamp              | `int` | Seconds since January 1, 1970.                               |
| Appointment count      | `int` |                                                              |
| Appointment timestamps | `int` | _Appointment count_ many appointment timestamps. Same format as _Timestamp_. |
| Sync interval          | `int` | The app’s current synchronisation interval, in minutes.      |
| Battery charge         | `int` | Mobile’s current battery level. Negative if the device is currently being charged. The absolute is always between 0 and 100. |

