# Contract Watch - Use Cases Implemented

Based directly on the supplied problem statement:

1. Add Contract
   - Stores contract name, vendor, start date, end date and renewal notice period.

2. Auto-Flag Renewal Review
   - The service calculates the boundary as `endDate - renewalNoticePeriodDays`.
   - On the exact boundary date, an active contract is flagged.
   - The scheduled service checks every 60 seconds while the application is running.

3. Log Renewal Decision
   - `RENEWED`: requires a new end date after the old end date.
   - `TERMINATED`: changes the contract status to TERMINATED and clears the reminder flag.
   - Each decision is stored as renewal history.

4. View Contracts Expiring in Next 30 Days
   - Active contracts with end dates from today through today + 30 days are returned.

5. Attach and View Document References
   - The project stores document URLs or local file-path references against a contract.

Business rules enforced in the service layer:

- Notice-period renewal flag is generated exactly at the boundary date.
- Terminated contracts cannot appear in active renewal reminders.
- A terminated contract cannot receive another renewal decision.
- A renewed contract must receive a new end date after the current end date.
- A terminated decision cannot contain a new end date.
- Invalid date ranges and impossible notice periods are rejected immediately.
