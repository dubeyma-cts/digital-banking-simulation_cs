import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FundTransferResponse } from '../../services/fund-transfer.service';

@Component({
  selector: 'app-fund-transfer-success',
  templateUrl: './fund-transfer-success.component.html',
  styleUrls: ['./fund-transfer-success.component.css']
})
export class FundTransferSuccessComponent implements OnInit {
  transfer: FundTransferResponse | null = null;

  constructor(private router: Router) {}

  ngOnInit(): void {
    const state = history.state as { transfer?: FundTransferResponse };
    this.transfer = state?.transfer || null;
    if (!this.transfer) {
      this.router.navigate(['/landing']);
    }
  }

  newTransfer(): void {
    this.router.navigate(['/fund-transfers']);
  }
}
