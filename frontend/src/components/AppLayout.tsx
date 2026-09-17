import { useState } from "react";
import { Outlet } from "react-router-dom";
import { DataProvider } from "../data/DataContext";
import type { ProductType } from "../types";
import { Topbar } from "./Topbar";

export interface ComplaintFilter {
  searchTerm: string;
  productTypeFilter: ProductType | "";
}

export function AppLayout() {
  const [searchTerm, setSearchTerm] = useState("");
  const [productTypeFilter, setProductTypeFilter] = useState<ProductType | "">("");

  return (
    <DataProvider>
      <Topbar
        searchTerm={searchTerm}
        onSearchTermChange={setSearchTerm}
        productTypeFilter={productTypeFilter}
        onProductTypeFilterChange={setProductTypeFilter}
      />
      <main>
        <Outlet context={{ searchTerm, productTypeFilter } satisfies ComplaintFilter} />
      </main>
    </DataProvider>
  );
}
